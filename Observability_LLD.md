# Chain Executor Observability Module - Low Level Design

## Table of Contents
1. [Overview](#overview)
2. [Design Goals](#design-goals)
3. [Architecture](#architecture)
4. [Observation Configuration](#observation-configuration)
5. [AOP Implementation](#aop-implementation)
6. [Sink Architecture](#sink-architecture)
7. [Thread Safety & Concurrency](#thread-safety--concurrency)
8. [Payload Ordering](#payload-ordering)
9. [Module Structure](#module-structure)
10. [Build Configuration](#build-configuration)
11. [Client Integration](#client-integration)
12. [Future Enhancements](#future-enhancements)

---

## Overview

The observability module provides **method-level tracing** for the chain-executor library using **Aspect-Oriented Programming (AOP)**. It captures method invocations, input parameters, return values and exceptions with minimal impact on business logic.

---

## Design Goals

| Goal | Description |
|------|-------------|
| **Minimal Client Changes** | Clients should not need extensive configuration |
| **Non-Blocking** | Observation should not impact execution performance |
| **Configurable Depth** | Support different levels of observation detail |
| **Extensible Sinks** | Support multiple output destinations (logs, storage, custom) |
| **Thread-Safe** | Handle concurrent method invocations safely |
| **Ordered Delivery** | Maintain chronological order of observations per sink |

---

## Architecture

### High-Level Flow

```
┌─────────────────────┐     ┌─────────────────────┐     ┌─────────────────────┐
│   Client Service    │     │  Chain Executor     │     │   Observation       │
│                     │────▶│  Core Module        │────▶│   Sinks             │
│                     │     │  (AOP Woven)        │     │                     │
└─────────────────────┘     └─────────────────────┘     └─────────────────────┘
                                     │
                                     ▼
                            ┌─────────────────────┐
                            │  Observation Aspect │
                            │  - Before payload   │
                            │  - After payload    │
                            └─────────────────────┘
```

### Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        chain-executor-core                              │
├────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐  │
│  │ StageExecution   │    │ @ObservedMethod  │    │ Observation      │  │
│  │ Manager          │◄───│ Annotation       │    │ Aspect           │  │
│  └──────────────────┘    └──────────────────┘    └────────┬─────────┘  │
│                                                           │            │
│  ┌──────────────────┐    ┌──────────────────┐            │            │
│  │ Observability    │◄───│ Observation      │◄───────────┘            │
│  │ Manager          │    │ Payload          │                         │
│  └────────┬─────────┘    └──────────────────┘                         │
│           │                                                            │
│           ▼                                                            │
│  ┌──────────────────────────────────────────┐                         │
│  │              Sinks                        │                         │
│  │  ┌────────┐  ┌────────┐  ┌────────────┐  │                         │
│  │  │Log Sink│  │Storage │  │Client      │  │                         │
│  │  │        │  │Sink    │  │Dispatch    │  │                         │
│  │  └────────┘  └────────┘  └────────────┘  │                         │
│  └──────────────────────────────────────────┘                         │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Observation Configuration

### Observation Scope vs Verbosity

Two orthogonal configuration dimensions:

| Dimension | Purpose | Values |
|-----------|---------|--------|
| **Scope** | Which methods to observe | `CRITICAL`, `STANDARD`, `INTERNAL` |
| **Verbosity** | How much detail to capture | `MINIMAL`, `STANDARD`, `VERBOSE` |

### Observation Scope

| Scope | Methods Included |
|-------|------------------|
| `CRITICAL` | Only critical public methods (`execute`, `resume`, `initNext`) |
| `STANDARD` | All public methods |
| `INTERNAL` | All methods including private/internal |

### Observation Verbosity

| Verbosity | Data Captured |
|-----------|---------------|
| `MINIMAL` | Stage, method name, status, timestamp |
| `STANDARD` | + Request params, response context |
| `VERBOSE` | + Invoker details, thread info, full stack context |

### Configuration Example

```java
ChainExecutorObservationConfig config = ChainExecutorObservationConfig.builder()
    .enabled(true)
    .scope(ObservationScope.STANDARD)
    .verbosity(ObservationVerbosity.STANDARD)
    .threadPoolSize(4)
    .enabledSinks(Set.of(
        LogSinkConfiguration.builder()
            .logLevel(Level.INFO)
            .build()
    ))
    .build();

ChainExecutorObservabilityManager.init(config);
```

---

## AOP Implementation

### Annotation: `@ObservedMethod`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObservedMethod {
    String name() default "";
    MethodCriticality criticality() default MethodCriticality.LOW;
}
```

### Method Criticality

| Criticality | When to Use |
|-------------|-------------|
| `CRITICAL` | Core public API methods |
| `MEDIUM` | Important internal methods |
| `LOW` | Helper/utility methods |

### Aspect Implementation

```java
@Aspect
@Slf4j
public class ChainExecutorObservationAspect {

  @Pointcut("execution(* com.github.nishgpt.chainexecutor.core.execution.StageExecutionManager.*(..))")
  public void executionManagerMethodCalled() {}

  @Pointcut("@annotation(com.github.nishgpt.chainexecutor.models.observability.ObservedMethod)")
  public void observedMethodCalled() {}

  @Around("executionManagerMethodCalled() && observedMethodCalled()")
  public Object aroundExecutionManagerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    // Prepare and dispatch BEFORE payload
    final var beforePayload = prepareBeforePayload(...);
    ChainExecutorObservabilityManager.dispatch(beforePayload);

    try {
      final Object response = joinPoint.proceed();
      
      // Prepare and dispatch AFTER payload (success)
      final var afterPayload = prepareAfterPayload(..., ExecutionOutcome.SUCCESS);
      ChainExecutorObservabilityManager.dispatch(afterPayload);
      
      return response;
    } catch (Throwable t) {
      // Prepare and dispatch AFTER payload (failure)
      final var afterPayload = prepareAfterPayload(..., ExecutionOutcome.FAILURE);
      ChainExecutorObservabilityManager.dispatch(afterPayload);
      throw t;
    }
  }
}
```

### Extracting Method Information from JoinPoint

| Information | How to Extract |
|-------------|----------------|
| Method name | `joinPoint.getSignature().getName()` |
| Arguments | `joinPoint.getArgs()` |
| Target instance | `joinPoint.getTarget()` |
| Declaring class | `joinPoint.getSignature().getDeclaringType()` |
| Parameter names | `((MethodSignature) joinPoint.getSignature()).getParameterNames()` |

### Accessing Private Fields (e.g., `executorFactory`)

Use cached `MethodHandle` for near-native performance:

```java
private static final MethodHandle EXECUTOR_FACTORY_GETTER;

static {
  try {
    Field field = StageExecutionManager.class.getDeclaredField("executorFactory");
    field.setAccessible(true);
    EXECUTOR_FACTORY_GETTER = MethodHandles.lookup().unreflectGetter(field);
  } catch (Exception e) {
    throw new ExceptionInInitializerError(e);
  }
}

// Usage
StageExecutorFactory factory = (StageExecutorFactory) EXECUTOR_FACTORY_GETTER.invoke(target);
```

| Approach | Overhead per Call |
|----------|-------------------|
| Uncached reflection | ~1-5 µs |
| Cached reflection | ~50-200 ns |
| Cached MethodHandle | ~10-30 ns |
| Direct getter | ~5-20 ns |

---

## Sink Architecture

### Sink Types

| Sink | Description | Client Implementation Required |
|------|-------------|--------------------------------|
| **LogSink** | Logs observations based on configured level | No |
| **ClientDispatchSink** | Invokes client-provided `ObservationProcessor` | Yes (implement interface) |
| **StorageSink** | Persists to storage (e.g., Aerospike) | No (provide storage client) |

### Sink Interface

```java
public interface ObservationSink {
    void consume(ChainExecutorObservationPayload payload);
}
```

### Dynamic Log Level

Using SLF4J 2.0+ fluent API:

```java
public class LogSink implements ObservationSink {
    private final Level logLevel;
    
    @Override
    public void consume(ChainExecutorObservationPayload payload) {
        log.atLevel(logLevel).log("Observation: {}", payload);
    }
}
```

### Sink Configuration via Visitor Pattern

```java
public interface ObservationSinkConfigurationVisitor<T> {
    T visit(LogSinkConfiguration configuration);
    T visit(ClientDispatchSinkConfiguration configuration);
    T visit(StorageSinkConfiguration configuration);
}
```

---

## Thread Safety & Concurrency

### Immutable State Pattern

Wrap all related state in an immutable record and swap atomically:

```java
record ObservabilityState(
    ChainExecutorObservationConfig config,
    Set<ObservationSink> sinks,
    Map<ObservationSink, ExecutorService> executors
) {
    static ObservabilityState empty() {
        return new ObservabilityState(null, Set.of(), Map.of());
    }
}

public class ChainExecutorObservabilityManager {
    private static final AtomicReference<ObservabilityState> state =
        new AtomicReference<>(ObservabilityState.empty());

    public static void applyConfig(ChainExecutorObservationConfig config) {
        // Build new state
        Set<ObservationSink> newSinks = buildSinks(config);
        Map<ObservationSink, ExecutorService> newExecutors = buildExecutors(newSinks);
        
        // Atomically swap
        ObservabilityState oldState = state.getAndSet(
            new ObservabilityState(config, newSinks, newExecutors)
        );
        
        // Cleanup old executors
        oldState.executors().values().forEach(ExecutorService::shutdown);
    }
}
```

## Payload Ordering

### Problem

Nested method calls produce payloads in wrong order if dispatched after method completion:

```
execute() calls initNext() internally

Wrong order (if queued):
1. initNext() BEFORE
2. initNext() AFTER
3. execute() BEFORE
4. execute() AFTER

Correct order (immediate dispatch):
1. execute() BEFORE
2. initNext() BEFORE
3. initNext() AFTER
4. execute() AFTER
```

### Solution: Immediate Dispatch

Dispatch each payload immediately after preparation:

```java
// BEFORE payload - dispatch immediately
dispatch(beforePayload);

try {
    Object result = joinPoint.proceed();
    // AFTER payload - dispatch immediately
    dispatch(afterPayload);
    return result;
} catch (Throwable t) {
    // AFTER payload - dispatch immediately
    dispatch(afterPayload);
    throw t;
}
```

### Per-Sink Ordering with Single-Threaded Executors

Each sink gets a dedicated single-threaded executor:

```java
public static void registerSink(ObservationSink sink) {
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(10_000);
    ExecutorService executor = new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        queue,
        r -> new Thread(r, "obs-sink-" + sink.getClass().getSimpleName()),
        new ThreadPoolExecutor.DiscardOldestPolicy()
    );
    SINK_EXECUTORS.put(sink, executor);
}
```

| Benefit | Description |
|---------|-------------|
| Non-blocking | `submit()` returns immediately |
| Ordered per sink | Single thread processes sequentially |
| Parallel across sinks | Each sink has own executor |
| Graceful degradation | Drops oldest payloads under load |

---

## Module Structure

### Recommended Package Layout

```
chain-executor-models/
├── observability/
│   ├── ChainExecutorObservationConfig.java
│   ├── ChainExecutorObservationPayload.java
│   ├── ObservedMethod.java
│   ├── MethodCriticality.java
│   ├── ObservationScope.java
│   ├── ObservationVerbosity.java
│   ├── ObservationPhase.java          (BEFORE, AFTER)
│   └── ExecutionOutcome.java          (SUCCESS, FAILURE)

chain-executor-core/
├── execution/
│   ├── StageExecutionManager.java
│   ├── StageExecutor.java
│   ├── StageChainRegistry.java
│   └── StageExecutorFactory.java
└── observability/
    ├── ChainExecutorObservationAspect.java
    ├── ChainExecutorObservabilityManager.java
    └── sink/
        ├── ObservationSink.java
        └── impl/
            ├── LogSink.java
            ├── ClientDispatchSink.java
            └── StorageSink.java
```

---

## Build Configuration

### Lombok + AspectJ Compatibility

AspectJ compiler (AJC) doesn't support Lombok annotations directly. Solution: **Delombok before AspectJ compilation**.

### Maven Plugin Configuration

```xml
<build>
  <plugins>
    <!-- Step 1: Delombok -->
    <plugin>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok-maven-plugin</artifactId>
      <version>1.18.20.0</version>
      <executions>
        <execution>
          <phase>generate-sources</phase>
          <goals>
            <goal>delombok</goal>
          </goals>
          <configuration>
            <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
            <outputDirectory>${project.build.directory}/delombok</outputDirectory>
            <addOutputDirectory>false</addOutputDirectory>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <!-- Step 2: Skip default compiler -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <skipMain>true</skipMain>
        <skip>true</skip>
      </configuration>
    </plugin>

    <!-- Step 3: AspectJ compile delomboked sources -->
    <plugin>
      <groupId>dev.aspectj</groupId>
      <artifactId>aspectj-maven-plugin</artifactId>
      <version>1.14</version>
      <configuration>
        <complianceLevel>17</complianceLevel>
        <source>17</source>
        <target>17</target>
        <sources>
          <source>
            <basedir>${project.build.directory}/delombok</basedir>
            <includes>
              <include>**/*.java</include>
            </includes>
          </source>
        </sources>
      </configuration>
      <executions>
        <execution>
          <phase>process-sources</phase>
          <goals>
            <goal>compile</goal>
          </goals>
        </execution>
      </executions>
      <dependencies>
        <dependency>
          <groupId>org.aspectj</groupId>
          <artifactId>aspectjtools</artifactId>
          <version>${aspectj.version}</version>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```

### AspectJ Plugin Notes

| Consideration | Recommendation |
|---------------|----------------|
| Plugin groupId | Use `dev.aspectj` (maintained fork) for Java 17+ |
| aspectjtools version | 1.9.21+ for Java 17 support |
| Weaving type | Compile-time (no client changes needed) |

---

## Client Integration

### Client POM

```xml
<dependency>
    <groupId>com.github.nishgpt</groupId>
    <artifactId>chain-executor-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Client Initialization

```java
// Minimal setup with logging
ChainExecutorObservabilityManager.init(
    ChainExecutorObservationConfig.builder()
        .enabled(true)
        .enabledSinks(Set.of(
            LogSinkConfiguration.builder()
                .logLevel(Level.INFO)
                .build()
        ))
        .build()
);

// With custom processor
ChainExecutorObservabilityManager.init(
    ChainExecutorObservationConfig.builder()
        .enabled(true)
        .enabledSinks(Set.of(
            ClientDispatchSinkConfiguration.builder()
                .processor(payload -> {
                    // Custom processing logic
                })
                .build()
        ))
        .build()
);
```

### No Additional Client Requirements

| Concern | Client Action |
|---------|---------------|
| AspectJ plugin | Not needed (compile-time weaving done in library) |
| Delombok | Not needed |
| `aspectjrt` dependency | Transitive (automatic) |

---

## Future Enhancements

### TODO Items

1. **GRANULAR Observation Depth** - Support for all internal method invocations with parent-child context
2. **Storage Sink Implementation** - Persist observations to Aerospike or similar
3. **Visualization Bundle** - Dropwizard bundle exposing API for observation data visualization
4. **Exception Handling Strategy** - Ensure observation errors never impact business execution
5. **Metrics Integration** - Expose observation metrics (counts, latencies) via Micrometer

### Parent-Child Context for GRANULAR Depth

For nested method tracking, maintain execution context:

```java
private static final ThreadLocal<Deque<String>> EXECUTION_STACK = 
    ThreadLocal.withInitial(ArrayDeque::new);

// In aspect
String parentObservationId = EXECUTION_STACK.get().peek();
String currentObservationId = UUID.randomUUID().toString();
EXECUTION_STACK.get().push(currentObservationId);

try {
    // Build payload with parentObservationId reference
    return joinPoint.proceed();
} finally {
    EXECUTION_STACK.get().pop();
}
```

---

## Version History

| Version | Changes |
|---------|---------|
| 0.0.6 | Initial release (production) |
| 0.1.0-SNAPSHOT | Observability module, module separation, license formatting |

---

## References

- [AspectJ Documentation](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)
- [SLF4J 2.0 Fluent API](https://www.slf4j.org/manual.html#fluent)
- [Guava EventBus](https://github.com/google/guava/wiki/EventBusExplained)

