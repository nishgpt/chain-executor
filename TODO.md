1. [ ] observation listeners, with exception handling
2. [x] Singular layer for registering and deregistering listeners, with support for multiple listeners.
3. [x] core logic of constructing the observation payload and dispatching to listeners
4. [x] Adding logic for parent and child method invocations when using GRANULAR observation depth - solved via group id
5. [ ] Facilitate the validation for the observation configuration
6. [ ] Check wherever observation error exception is absolutely needed or we can just log the error and move on, to avoid any unintended consequences of throwing exceptions from observation listeners.
7. [ ] Ordering of observation for a specific sink should be ensured - use sink based single threaded executors with a bounded-queue to ensure ordering of events for a specific sink, while allowing parallel processing across different sinks.
8. [ ] Update the cached stage executor factory impl to MethodHandle instead of Field
9. [ ] Invoker details possible to get ?
10. [ ] When enabling storage sink, need to check how to storage will look like, as stage or execution context id may not be available for all observations.
11. [ ] Check if we can remove lombok dependency in core module and use explicit getters/setters/constructors in the codebase to avoid any issues with AspectJ.
12. [ ] execution id for observations where execution context is present.
13. [ ] While logging is it possible to pass the traceId also to correlate the logs with the execution flow in the service.
14. [ ] private method access modifiers at relevant places for ObservationPayloadBuilder
15. [ ] Make relevant DTOs as record
16. [ ] Update LLD doc with the finalized design and implementation details.

~~Issues observed on testing : to be fixed~~ 
~~1. Fields coming null :~~
   ~~1. ExecutionContext (deep copy issue, no matching constructors)~~
   ~~2. StageStatus - either of the above necessary items are null~~
~~Fix Extraction of Stage-
   ~~1. first check if the stage executor is present and extract executorKeys and get one and then get stage from the executorKey~~
   ~~2. if there is no stage executor then extract it from method params~~~~
~~Check the logic of passing stageExecutorKey to methods like performPostCompletionSteps, the previous executor key is getting passed.~~