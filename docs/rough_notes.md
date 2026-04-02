Rough idea/notes

1. As of now, users of chain-executor expose an api which gives the latest snapshot of the entire workflow, from this oncalls/devs get to see only the current state of the application and not how the user moved along the journey.
2. To be able to debug an entire journey, oncalls have to browse application update events through events-system and grep through logs from some reference timestamp and search for application id / mid / userid etc. and then track manually on how the user moved, what exceptions were encountered and back and forth happened.
3. The idea is to come up with an observability module in chain-executor framework, which can track at a user/application level what stage was invoked with what action (execute, resume, sync) at what time along with who invoked it. Idea is to track it along with the request level information, pre execution context and post execution context.
4. The module may use storage of the underlying service to make sure data resides locally and does not pose any compliance issues.
5. This idea can be extended to other used workflow frameworks like Nornet as well.
6. Why not spyglass? - what spyglass does is an end to end flow tracking for a flow, across cases, without the underlying data. What we are aiming here is to actually give a visualisation along with data bags in play during the flow.
7. The plan is to start with a basic poc and see whether there is goodness.
8. possible sinks for the observability data 
   1. storage layer of the underlying client, aerospike mostly
   2. a method call which can be implemented by client to send it as events to events-monitoring system
   3. a default logger implementation, which will be provided as a part of the module, which will print these as debug logs.

Phases/versions with features

V0
1. Make chain-executor repo, a multi-module project with core and observability modules
2. Clients should have the option to enable Observability effortlessly, though it must remain entirely optional.
3. There should be several depth and verbosity of observability, where one can opt at what level they would like to observe the flow and with how much level of details.
4. Enable tracking at a granular id level and to be able to provide the default debug logs in this version. All clients will have the debug logs by default enabled as a part of this feature. More sinks (storage, method call) will be taken up in further versions, depending on the goodness and feedback from clients.
5. Method calls to a handler which can be implemented by the client

V1
If V0 works well and has goodness, V1 will incorporate the following
1. If there is a need for visualisation, have storage sink, where the data can reside in storage layer of the client itself
2. Relevant apis for serving the observability data from storage
3. Provide a configurable ttl from last updated at for storing this data and flow trace
4. Provide configuration for partial tracking / stage(s) wise tracking and other similar features
5. Enable / disable tracking across sinks in runtime for data volume intense clients

V2
Once storage sink is enabled, below items can be incorporated in V2
1. Visualisation for a case where oncall can view what happened in the flow, along with the data


Pros
1. Clients (primarily the LOS systems of ML, CL, SL) can shave off some of the redundant logs regarding the chain executor flow.
2. If storage sink is enabled it can help build confidence with the above observability and log less from the application side if the use-case for debugging is along the lines of application lifecycle (30-60days).
3. Better productivity for L1-oncall if the use-case is to quickly debug what happened with the user along with the actions performed, and remove dependencies from application generated events and logs.
4. When live debugger mode is supported and enabled it can help figure out issues during runtime much faster.

Cons
1. Folks who enable storage sink will see a bump / spike in data storage but with a rightly configured TTL they wont see long term issues