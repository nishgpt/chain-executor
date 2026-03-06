1. observation listeners, with exception handling 
2. ~~singular layer for registering and deregistering listeners, with support for multiple listeners.~~
3. core logic of constructing the observation payload and dispatching to listeners
4. Adding logic for parent and child method invocations when using GRANULAR observation depth
5. Facilitate the validation for the observation configuration
6. Check wherever observation error exception is absolutely needed or we can just log the error and move on, to avoid any unintended consequences of throwing exceptions from observation listeners.
7. Ordering of observation for a specific sink should be ensured - use sink based single threaded executors with a bounded-queue to ensure ordering of events for a specific sink, while allowing parallel processing across different sinks.
8. Update LLD doc with the finalized design and implementation details.
9. Update the cached stage executor factory impl to MethodHandle instead of Field
10. Invoker details possible to get ?
11. When enabling storage sink, need to check how to storage will look like, as stage or execution context id may not be available for all observations.