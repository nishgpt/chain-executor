# Chain Executor Observability
## Feature Demo & Overview

---

## 📋 Table of Contents

1. [Introduction](#introduction)
2. [Motivation & Problem Statement](#motivation--problem-statement)
3. [How It Helps](#how-it-helps)
4. [How It Works](#how-it-works)
5. [Integration Guide](#integration-guide)
6. [Configuration Options](#configuration-options)
7. [Roadmap](#roadmap)
8. [Summary](#summary)

---

## Introduction

The **Chain Executor Observability** is a new feature designed to provide method-level tracing and debugging capabilities for applications using the chain-executor library. It enables developers and on-call engineers to gain visibility into the execution flow of staged workflows without requiring manual log instrumentation.

**Current Version:** `0.1.0-OB-ALPHA5` (Alpha - Ready for internal testing)

---

## Motivation & Problem Statement

### Current Challenges

| Challenge | Description                                                                                                                                                   |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Limited Visibility** | Clients expose APIs showing only the current state of a workflow, not the journey taken to reach that state                                                   |
| **Manual Log Hunting** | Debugging requires browsing application events through Foxtrot, grepping through logs, and manually tracking user journeys using reference timestamps and IDs |
| **Time-Consuming Debugging** | On-calls need to correlate multiple data sources (logs, events, application state) to understand what happened during execution                               |
| **No Journey Tracking** | No out-of-the-box solution to track what stage was invoked, with what action (execute, resume, initNext), at what time, and by whom                           |

### Real-World Scenario

> *"A user reports an issue with their loan application. The on-call engineer needs to understand:*
> - *What stages did the user pass through?*
> - *What was the context at each stage?*
> - *Where did the execution fail or behave unexpectedly?*
> - *What were the input parameters and responses?*
>
> *Currently, this requires manually correlating logs, events, and database state across multiple systems."*

---

## How It Helps

### Benefits

| Benefit | Description |
|---------|-------------|
| 🔍 **End-to-End Journey Visibility** | Track the complete execution flow of a workflow, including all stages, actions, and transitions |
| ⏱️ **Faster Debugging** | Reduce time spent correlating logs and events - all execution data is captured in structured observation payloads |
| 📊 **Contextual Data Capture** | Capture request parameters, execution context, and response data alongside execution flow |
| 🎛️ **Configurable Depth & Verbosity** | Choose how much detail to capture based on your debugging needs - from basic stage transitions to full context dumps |
| 🔌 **Multiple Output Destinations** | Send observations to logs, custom processors, or storage (future) based on your infrastructure |
| 🚀 **Minimal Integration Effort** | Enable observability with just configuration changes - no code modifications required |
| 📉 **Reduced Logging Overhead** | Potentially reduce redundant application logs once observability captures the execution flow |

[//]: # ()
[//]: # (### Who Benefits?)

[//]: # ()
[//]: # (| Role | How They Benefit |)

[//]: # (|------|------------------|)

[//]: # (| **Developers** | Understand execution flow during development and testing without adding debug logs |)

[//]: # (| **L1 On-Calls** | Quickly diagnose user issues by viewing the complete execution journey |)

[//]: # (| **L2 Engineers** | Deep-dive into execution context and parameters for complex issues |)

[//]: # (| **Product Teams** | Gain insights into how users move through workflows |)

### Trade-offs to Consider

| Consideration | Impact |
|---------------|--------|
| **Memory Overhead** | Observation payloads are created for each method invocation (mitigated by configurable depth/verbosity) |
| **Storage Costs** | If storage sink is enabled, data volume will increase (configurable TTL planned) |
| **Thread Pool Usage** | Dedicated thread pool for observation dispatch (configurable size) |

---

## How It Works

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Client Application                                │
│                                                                             │
│   ┌─────────────────┐         ┌─────────────────┐                           │
│   │  Your Service   │────────▶│  Chain Executor │                           │
│   │                 │         │  (with AOP)     │                           │
│   └─────────────────┘         └────────┬────────┘                           │
│                                        │                                    │
│                                        ▼                                    │
│                           ┌────────────────────────┐                        │
│                           │  Observation Aspect    │                        │
│                           │  ┌──────────────────┐  │                        │
│                           │  │ Intercept Method │  │                        │
│                           │  │ ───────────────▶ │  │                        │
│                           │  │ Capture BEFORE   │  │                        │
│                           │  │ ───────────────▶ │  │                        │
│                           │  │ Execute Method   │  │                        │
│                           │  │ ───────────────▶ │  │                        │
│                           │  │ Capture AFTER    │  │                        │
│                           │  └──────────────────┘  │                        │
│                           └───────────┬────────────┘                        │
│                                       │                                     │
│                                       ▼                                     │
│                           ┌────────────────────────┐                        │
│                           │  Observability Manager │                        │
│                           │  ┌──────────────────┐  │                        │
│                           │  │ Async Dispatch   │  │                        │
│                           │  │ to Sinks         │  │                        │
│                           │  └──────────────────┘  │                        │
│                           └───────────┬────────────┘                        │
│                                       │                                     │
│               ┌───────────────────────┼───────────────────────┐             │
│               ▼                       ▼                       ▼             │
│        ┌───────────┐           ┌───────────┐           ┌───────────┐        │
│        │  Log Sink │           │  Custom   │           │  Storage  │        │
│        │  (Built-  │           │  Sink     │           │  Sink     │        │
│        │   in)     │           │  (Client) │           │  (Future) │        │
│        └───────────┘           └───────────┘           └───────────┘        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### What is AOP (Aspect-Oriented Programming)?

AOP is a programming paradigm that allows you to add behavior to existing code **without modifying the code itself**. Think of it as "interceptors" that wrap around your methods.

**Key Concepts:**
- **Aspect**: A module containing cross-cutting logic (in our case, observation capture)
- **Pointcut**: Defines which methods to intercept (in our case, methods annotated with `@ObservedMethod`)
- **Advice**: The action taken at the join point (in our case, capture before/after payloads)

**Why AOP for Observability?**
- ✅ **Non-invasive**: No changes to business logic code
- ✅ **Centralized**: All observation logic in one place
- ✅ **Compile-time weaving**: No runtime reflection overhead (we use AspectJ)
- ✅ **Selective**: Only annotated methods are observed

### Observation Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Method Execution Flow                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Method called (e.g., execute())                             │
│           │                                                     │
│           ▼                                                     │
│  2. Aspect intercepts ─────────────────┐                        │
│           │                            │                        │
│           ▼                            ▼                        │
│  3. Extract stage, context,    4. Build BEFORE payload          │
│     executor info                      │                        │
│           │                            │                        │
│           │                            ▼                        │
│           │                    5. Dispatch to sinks (async)     │
│           │                                                     │
│           ▼                                                     │
│  6. Proceed with actual method execution                        │
│           │                                                     │
│           ▼                                                     │
│  7. Method completes (success/failure)                          │
│           │                                                     │
│           ▼                                                     │
│  8. Build AFTER payload ─────▶ 9. Dispatch to sinks (async)     │
│           │                                                     │
│           ▼                                                     │
│  10. Return result to caller                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Observation Payload Structure

Each observation captures:

| Field | Description |
|-------|-------------|
| `observationId` | Unique ID for this specific observation |
| `observationGroupId` | Links BEFORE and AFTER observations for the same method call |
| `phase` | `BEFORE_METHOD_INVOCATION` or `AFTER_METHOD_INVOCATION` |
| `methodName` | Name of the method being observed (e.g., `execute`, `resume`) |
| `stage` | Current stage in the workflow |
| `stageStatus` | Status of the stage at observation time |
| `timestampInMillis` | When the observation was captured |
| `context` | Execution context (if VERBOSE verbosity) |
| `request` | Stage execution request (if VERBOSE verbosity) |
| `outcome` | `SUCCESS` or `FAILURE` (AFTER payload only) |
| `exception` | Exception details if failed (AFTER payload only) |

---

## Integration Guide

### Prerequisites
1. The child class of ExecutionContext should have a constructor with all fields as parameters, to facilitate deep copy of the context for capturing in observation payloads. This is required only if you want to capture execution context in the observations (VERBOSE verbosity level).

### Step 1: Add Dependency

```xml
<dependency>
    <groupId>com.github.nishgpt</groupId>
    <artifactId>chain-executor-core</artifactId>
    <version>0.1.0-OB-ALPHA5</version>
</dependency>
```

### Step 2: Initialize Observability Manager

During application startup, initialize the observability manager with your configuration:

```java
// Minimal setup - just logging
ChainExecutorObservabilityManager.init(
    ChainExecutorObservationConfig.builder()
        .defaultConfigParams(ObservationConfigParams.builder()
            .enabled(true)
            .depth(ObservationDepth.STANDARD)
            .verbosity(ObservationVerbosity.BASIC)
            .build())
        .enabledSinks(Set.of(
            LogSinkConfiguration.builder()
                .logLevel(LogLevel.DEBUG)
                .build()
        ))
        .build(),
    objectMapper,    // Your application's ObjectMapper
    injector         // Guice injector (for custom sinks)
);
```

### Step 3: (Optional) Custom Sink Implementation

If you want to process observations in your own way:

**a. Create a custom sink class:**

```java
@ChainExecutorObserver
@Singleton
public class MyObservationProcessor implements ObservationSink {
    
    @Override
    public void consume(ObservationPayload payload) {
        // Send to Foxtrot, metrics system, or any custom destination
        foxtrotClient.send(convertToEvent(payload));
    }
}
```

**b. Configure the custom sink:**

```java
ChainExecutorObservationConfig.builder()
    .enabledSinks(Set.of(
        CustomSinkConfiguration.builder()
            .observerPackage("com.mycompany.myservice.observers")
            .build()
    ))
    .build()
```

### That's It! 🎉

**No other changes required.** The AOP weaving is done at compile-time within the library. Your application code remains unchanged.

---

## Configuration Options

### Observation Depth

Controls **which methods** are observed:

| Depth | Methods Observed |
|-------|------------------|
| `STANDARD` | Only critical methods (`execute`, `resume`, `initNext`, etc.) |
| `GRANULAR` | All methods including internal helpers (more verbose, higher overhead) |

### Observation Verbosity

Controls **how much data** is captured:

| Verbosity | Data Captured |
|-----------|---------------|
| `BASIC` | Stage, method name, status, timestamp |
| `VERBOSE` | + Execution context, request params, chain identifier, exception details |

### Stage-wise Configuration

Configure different settings for different stages:

```java
ChainExecutorObservationConfig.builder()
    .defaultConfigParams(ObservationConfigParams.builder()
        .enabled(true)
        .depth(ObservationDepth.STANDARD)
        .verbosity(ObservationVerbosity.BASIC)
        .build())
    .stageWiseConfigParams(Map.of(
        MyStage.CRITICAL_STAGE, ObservationConfigParams.builder()
            .enabled(true)
            .depth(ObservationDepth.GRANULAR)
            .verbosity(ObservationVerbosity.VERBOSE)
            .build(),
        MyStage.LOW_PRIORITY_STAGE, ObservationConfigParams.builder()
            .enabled(false)
            .build()
    ))
    .build()
```

### Available Sinks

| Sink | Status | Description |
|------|--------|-------------|
| **LogSink** | ✅ Available | Logs observations at configurable level (DEBUG/INFO/TRACE) |
| **CustomSink** | ✅ Available | Client implements `ObservationSink` interface for custom processing |
| **StorageSink** | 🚧 Planned | Persist observations to storage (Aerospike) for later visualization |

### Runtime Configuration Refresh

Update configuration without restarting:

```java
// Call this when configuration changes
ChainExecutorObservabilityManager.refreshConfig(newConfig);
```

---

## Roadmap

### Current Version (V0 - Alpha)

| Feature | Status |
|---------|--------|
| AOP-based method observation | ✅ Complete |
| Log sink | ✅ Complete |
| Custom sink (client-implemented) | ✅ Complete |
| Configurable depth & verbosity | ✅ Complete |
| Stage-wise configuration | ✅ Complete |
| Runtime config refresh | ✅ Complete |
| Observation grouping for correlated payloads | ✅ Complete |

### Upcoming - V1

| Feature                     | Description                                                                       |
|-----------------------------|-----------------------------------------------------------------------------------|
| Origin of method invocation | Captures details around which client class and method invoked the observed method |
| Storage sink                | Persist observations to Aerospike for long-term storage                           |
| Visualization APIs          | REST APIs to query and retrieve observation data                                  |
| Configurable TTL            | Auto-cleanup of observation data based on retention policy                        |
| Per-sink ordering           | Ensure chronological order within each sink                                       |

### Future - V2

| Feature | Description                                                                                      |
|---------|--------------------------------------------------------------------------------------------------|
| Visual dashboard | UI (could be exposed to Ops team) to visualize execution flow and journey backed by storage sink |

### Known TODOs & Improvements

- Aspect on StageExecutor methods e.g sync()
- Enhanced validation for observation configuration
- Exception handling improvements to ensure observation errors don't impact business execution
- Compact ID generation for observation IDs
- Support for traceId correlation with existing logging infrastructure

---

## Summary

| Aspect | Details |
|--------|---------|
| **What** | Method-level tracing for chain-executor workflows |
| **Why** | Faster debugging, better visibility, reduced manual log hunting |
| **How** | AOP-based interception with async dispatch to multiple sinks |
| **Integration** | Add dependency + initialize configuration (no code changes) |
| **Current Status** | Alpha - ready for internal testing |
| **Next Steps** | Storage sink, visualization APIs, dashboard |

### Quick Start Checklist

- [ ] Add `chain-executor-core:0.1.0-OB-ALPHA5` dependency
- [ ] Initialize `ChainExecutorObservabilityManager` at startup
- [ ] Configure desired depth and verbosity
- [ ] Enable at least one sink (LogSink for basic setup)
- [ ] (Optional) Implement custom sink for advanced use cases

---

*Document Version: 1.0 | Last Updated: March 2026*

