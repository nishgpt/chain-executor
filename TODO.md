1. observation listeners, with exception handling 
2. singular layer for registering and deregistering listeners, with support for multiple listeners for same entity and action type.
3. core logic of constructing the observation payload and dispatching to listeners
4. Adding logic for parent and child method invocations when using GRANULAR observation depth
5. Facilitate the validation for the observation configuration