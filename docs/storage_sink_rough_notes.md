This document outlines the rough notes for enabling storage sink in chain-executor observability feature.

## Background
Chain-executor is a library capable of orchestrating the linear execution of a stage-based workflow

For the observability feature, each method in stage execution manager is aspected and observation payloads are prepared before and after method invocations. The same payloads are sent to several sinks (currently Log and Custom sinks).

The plan is to add another sink - StorageSink

## What will the sink do ?
The sink basically persists the observation payloads for a configured time window (or eternally).
The storage should be designed in a way that the chain-executor bundle (to be developed) can provide observation read api(s) to the client’s service out of the box and the same can be used to power UI/consoles to view observation payloads which are essentially workflow execution traces ordered chronologically for a user/case. The tracking/query id can be the contextId from observationPayload.

## How will the payloads be stored ?
Most of the chain-executor clients use aerospike as their primary storage layer and I would like to latch onto the same infra and resource rather than making it another heavy step for clients to procure a separate infrastructure. The bundle can take in the aerospike client, namespace and set level details to connect to the same aerospike. Of course there are certain caveats with the same, the heavy writes of observation payloads will make the aerospike utilisation (cpu, memory and disk) spike up, but that can be managed with fine tuned configurations of observation of certain critical stages, with properly defined TTL/expiry.

## What query patterns do I need ?
1. [MUST] Query by contextId - All the observationPayloads sent against a particular contextId should all be fetchable. The response can be paginated (either at storage layer or at the api layer) if the number of payloads are high.
2. [Good to have] Query by contextId and stage - Suppose the oncall wants to debug only a certain stage observation payload, we should be able to query and filter out the same. Filter if doable at the storage layer would be much better than doing it in the application layer.

## Key points to note during design of storage layer
1. Storage of observation payloads should be designed in such a way that, supporting the majority of the query patterns does not pose load on Aerospike, and does not do scans. Query is acceptable, but with proper indexes.
2. Batch reads or single lookups are much more preferable.
