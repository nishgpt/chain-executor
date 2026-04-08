# StorageSink LLD – chain-executor Observability

## Problem Statement
Add a `StorageSink` to the chain-executor observability feature that persists `ObservationPayload`s for a configurable TTL, enables chronological trace retrieval by `contextId`, and optionally filters by stage — without requiring secondary indexes or scans on Aerospike.

## Current State (as-is)
- `ObservationSink` interface: single method `consume(ObservationPayload)`
- Existing sinks: `LogSink`, `CustomSink`
- `StorageSink` visitor path exists in `ChainExecutorObservabilityManager.buildSinks()` but is a TODO
- `StorageSinkConfiguration` exists, holds a `StorageInfo` (only has `storeType`)
- `StorageInfo` is skeleton-only — no Lombok, no accessors, no Aerospike fields
- `StoreType` enum: `IN_MEMORY`, `AEROSPIKE`
- Dispatch is async (thread pool), so `consume()` must be thread-safe

---

## Atomicity Design Decision

**Problem:** Writing observation payloads in two steps (payload record + manifest record) creates a non-atomic operation that can't be solved with multi-record transactions in Aerospike < 8.x.

**Why single-record-per-contextId doesn't scale:**
A single record keyed by `contextId` holding all payloads inline hits Aerospike's default 1MB record size limit. With back-and-forth flow traversal, up to 1000 observations × ~3-5KB each = up to **5MB per contextId** — well over the limit.

**Solution: Two-set hybrid with best-effort atomicity**
Split *what is large* (full payload) from *what is small* (lightweight index metadata):

- **Manifest record** (key=`contextId`): CDT List of lightweight refs `{observationId, stage, phase, ts}` — ~150 bytes per entry. 1000 observations → **~150KB**, safely within 1MB.
- **Payload records** (key=`observationId`): full serialized `ObservationPayload` JSON, ~3-5KB each, stored as individual records — no aggregate size limit.

**Best-effort atomicity strategy (payload-first):**
1. Write payload record first (`put`, idempotent)
2. Append lightweight ref to manifest (`ListOperation.append`, single-record atomic)

If step 1 fails → nothing persisted, retry the entire write.
If step 2 fails → payload is orphaned (exists but invisible to contextId queries). Log a warning and retry the manifest append (configurable retries). Since `consume()` runs in an async observer thread pool (not the main execution thread), retries add no latency to the caller's critical path.

**Why payload-first (not manifest-first)?**
If the manifest append fails after the payload write, the read path simply sees fewer entries — clean degradation with no missing-record errors.
If the manifest were written first and payload write failed, the read path would need to handle dangling references on every query, adding complexity.

Worst-case failure (all retries exhausted): a single observation is silently dropped from storage. This is **acceptable for observability** — it is a best-effort diagnostic feature, not a business-critical transaction.

---

## Aerospike Data Model

### Set 1: Manifest Set (configurable name, e.g. `ce_obs_manifest`)
| Field | Value |
|---|---|
| Key | `contextId` (String) |
| Bin `refs` | CDT List of Maps (lightweight per-observation metadata) |
| TTL | Configurable via `AerospikeStorageInfo.ttlInSeconds` |

Each element in `refs`:
```
{
  "observationId"      → String  (~36 chars, UUID)
  "stage"              → String  (~20 chars)
  "phase"              → String  (BEFORE | AFTER)
  "timestampInMillis"  → long
}
```
**Max size:** 1000 observations × ~150 bytes = ~150KB ✅ (well within 1MB)

### Set 2: Payload Set (configurable name, e.g. `ce_obs_payload`)
| Field | Value |
|---|---|
| Key | `observationId` (String, UUID) |
| Bin `data` | Full serialized JSON of `ObservationPayload` |
| TTL | Same as manifest |

**Max size per record:** ~3-5KB ✅ (no concern, each observation is independent)

### Why this works for query patterns
1. **Query by contextId [MUST]:**
   - `get(manifestKey)` → 1 key lookup → get all `observationId`s from `refs`
   - `batchGet(observationIds[])` → 1 Aerospike batch call → all payloads in one round-trip
   - Sort by `timestampInMillis` → return

2. **Query by contextId + stage [Good to have]:**
   - Same manifest `get()` → filter `refs` by `stage` field **in-app, before any payload reads** (cheap — only 150-byte entries)
   - `batchGet` only the filtered `observationId`s → fetch only what's needed
   - This is **better than the single-record approach**: stage filtering reduces the batch read size

---

## Class Design

### chain-executor-models changes

#### 1. `StorageInfo` (abstract — refactor existing skeleton)
```java
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public abstract class StorageInfo {
    private StoreType storeType;
    private int ttlInSeconds;           // 0 = never expire
    public abstract <T> T accept(StorageInfoVisitor<T> visitor);
}
```

#### 2. NEW: `StorageInfoVisitor<T>` interface
```java
public interface StorageInfoVisitor<T> {
    T visit(AerospikeStorageInfo info);
    T visit(InMemoryStorageInfo info);
}
```

#### 3. NEW: `AerospikeStorageInfo extends StorageInfo`
```java
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class AerospikeStorageInfo extends StorageInfo {
    @NotBlank private String namespace;
    @NotBlank private String manifestSet;   // e.g. "ce_obs_manifest"
    @NotBlank private String payloadSet;    // e.g. "ce_obs_payload"
    @Builder.Default private int manifestWriteMaxRetries = 3;   // retries for manifest append on failure
}
```
> `IAerospikeClient` is NOT stored here — injected via Guice from client's service.
> Two sets are required: one for lightweight manifest records (keyed by contextId), one for full payload records (keyed by observationId).

#### 4. NEW: `InMemoryStorageInfo extends StorageInfo`
```java
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class InMemoryStorageInfo extends StorageInfo {
    // No additional fields needed for in-memory; TTL from parent is informational only
}
```

#### 5. Update `StorageSinkConfiguration`
- Jackson `@JsonSubTypes` on `StorageInfo` to support polymorphic deserialization of `AerospikeStorageInfo` / `InMemoryStorageInfo`
- Validate `storageInfo` is `@Valid @NotNull`

---

### chain-executor-core changes

#### 6. NEW: `ObservationStore` interface
Location: `core/observability/sink/storage/ObservationStore.java`
```java
public interface ObservationStore extends ObservationSink {
    List<ObservationPayload> getByContextId(String contextId);
    List<ObservationPayload> getByContextIdAndStage(String contextId, String stage);
}
```
> Extends `ObservationSink` so the store is both writer and reader — no separate sink wrapper needed.
> `String stage` is used (not `Stage` enum) to avoid generics complexity; callers can pass `stage.name()`.

#### 7. NEW: `AerospikeObservationStore implements ObservationStore`
Location: `core/observability/sink/storage/impl/AerospikeObservationStore.java`

**Constructor:** `(AerospikeStorageInfo config, IAerospikeClient aerospikeClient, ObjectMapper mapper)`

**`consume(ObservationPayload payload)` write flow (payload-first, best-effort):**
1. Serialize full payload to JSON string
2. `WritePolicy` with TTL from `config.ttlInSeconds`; `RecordExistsAction.PUT_ONLY` (idempotent on retry since key=observationId is a UUID — only written once)
3. Write payload record: `aerospikeClient.put(payloadKey, Bin("data", serializedJson))`
4. Build lightweight ref Map: `{observationId, stage, phase, timestampInMillis}`
5. Append ref to manifest record: `aerospikeClient.operate(manifestKey, ListOperation.append("refs", Value.get(refMap)))` — single-record operation, always atomic
6. On manifest append failure: retry up to `config.manifestWriteMaxRetries` times with brief back-off; log warning if all retries exhausted

**`getByContextId(String contextId)` read flow:**
1. `Record manifest = aerospikeClient.get(null, manifestKey)` — single key lookup
2. If null → return empty list
3. `List<Map> refs = (List<Map>) manifest.getList("refs")`
4. Extract all `observationId` strings from refs
5. `Record[] payloads = aerospikeClient.get(null, observationKeys[])` — **one batch call**
6. Deserialize each `payloads[i].getString("data")` into `ObservationPayload` (Jackson `@JsonTypeInfo` polymorphism); skip nulls (orphaned ref guard)
7. Sort by `timestampInMillis` → return

**`getByContextIdAndStage(String contextId, String stage)` read flow:**
1. Same manifest `get()` as above
2. **Filter** `refs` in-app where `ref.get("stage").equals(stage)` — O(n) over ~150-byte entries, no payload I/O yet
3. Extract filtered `observationId`s
4. `batchGet` only the filtered payload keys → much smaller batch than full query
5. Deserialize, sort, return

**Error handling:** Wrap Aerospike exceptions in a logged warning (non-critical path — observability should not blow up the caller).

#### 8. NEW: `InMemoryObservationStore implements ObservationStore`
Location: `core/observability/sink/storage/impl/InMemoryObservationStore.java`

**State:** `ConcurrentHashMap<String, CopyOnWriteArrayList<String>>` (contextId → list of serialized payloads)

**`consume(payload)`:** Serialize to JSON, append to list for `contextId`

**`getByContextId(contextId)`:** Deserialize all entries

**`getByContextIdAndStage(contextId, stage)`:** Deserialize all + filter by stage in-app

> Used for dev/testing. Thread-safe via `CopyOnWriteArrayList`.

#### 9. NEW: `StorageSinkFactory`
Location: `core/observability/sink/storage/StorageSinkFactory.java`

Utility for `ChainExecutorObservabilityManager` to build the right store from config:
```java
public class StorageSinkFactory {
    public static ObservationStore build(StorageSinkConfiguration config,
                                          Injector injector,
                                          ObjectMapper mapper) {
        return config.getStorageInfo().accept(new StorageInfoVisitor<ObservationStore>() {
            @Override
            public ObservationStore visit(AerospikeStorageInfo info) {
                IAerospikeClient client = injector.getInstance(IAerospikeClient.class);
                return new AerospikeObservationStore(info, client, mapper);
            }
            @Override
            public ObservationStore visit(InMemoryStorageInfo info) {
                return new InMemoryObservationStore(info, mapper);
            }
        });
    }
}
```

#### 10. Wire up in `ChainExecutorObservabilityManager`
- Replace the TODO in `buildSinks()` visitor for `StorageSinkConfiguration`:
  ```java
  ObservationStore store = StorageSinkFactory.build(configuration, injector, mapper);
  newSinks.add(store);
  ```
- Add `getObservationStore()` static method on `ChainExecutorObservabilityManager` for read access:
  ```java
  public static Optional<ObservationStore> getObservationStore() {
      return observabilityManagerState.get().sinks().stream()
          .filter(s -> s instanceof ObservationStore)
          .map(s -> (ObservationStore) s)
          .findFirst();
  }
  ```
  > This is the read API hook for the future `chain-executor-bundle`

---

## Validation additions
- `AerospikeStorageInfo`: `namespace` and `set` must be non-blank
- `StorageSinkConfiguration` validator: if `storeType == AEROSPIKE`, validate `AerospikeStorageInfo` fields
- `ChainExecutorObservabilityManager.validate()`: no new changes needed — `@Valid` on `storageInfo` handles it

---

## Aerospike Dependency
- `com.aerospike:aerospike-client` added to `pom.xml` (root `dependencyManagement`) as `optional` / `provided` scope
- Only `chain-executor-core/pom.xml` declares it as a dependency (optional)
- Client's service provides the actual `IAerospikeClient` bean — injected via Guice

---

## Package Layout (new files)
```
chain-executor-models/
  models/observability/config/sink/impl/
    StorageInfo.java                    ← refactor (add Lombok, abstract, visitor accept)
    AerospikeStorageInfo.java           ← NEW
    InMemoryStorageInfo.java            ← NEW
    StorageInfoVisitor.java             ← NEW

chain-executor-core/
  core/observability/sink/storage/
    ObservationStore.java               ← NEW
    StorageSinkFactory.java             ← NEW
    impl/
      AerospikeObservationStore.java    ← NEW
      InMemoryObservationStore.java     ← NEW
```

---

## Open Questions / Future Work
1. **Pagination:** `getByContextId` currently returns all payloads. Future: add cursor-based pagination (by `timestampInMillis`) as a separate overload, to be exposed via bundle's read API.
2. **Aerospike CDT Expression Filters:** Stage filtering on the manifest could be pushed server-side using CDT list filters (Aerospike client >= 5.x) — eliminates the in-app filter step for the stage query path.
3. **`chain-executor-bundle` module:** Will be a new Maven module that wires together `ObservationStore` + exposes REST/gRPC read APIs. Design is out of scope for this LLD.
4. **Orphaned payload cleanup:** Payload records with no manifest reference (due to exhausted retries) are naturally cleaned up by TTL expiry. No explicit GC needed.
5. **In-memory TTL eviction:** `InMemoryObservationStore` doesn't enforce TTL. Future: use Guava `CacheBuilder.expireAfterWrite()` keyed by `contextId` for TTL-based eviction.
