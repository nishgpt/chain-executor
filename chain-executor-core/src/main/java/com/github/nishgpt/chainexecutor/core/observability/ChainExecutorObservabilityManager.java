/*
 * Copyright(c) 2023 Nishant Gupta (nishant141077@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.nishgpt.chainexecutor.core.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nishgpt.chainexecutor.core.observability.sink.ObservationSink;
import com.github.nishgpt.chainexecutor.core.observability.sink.impl.LogSink;
import com.github.nishgpt.chainexecutor.models.error.ChainExecutorException;
import com.github.nishgpt.chainexecutor.models.error.ErrorCode;
import com.github.nishgpt.chainexecutor.models.observability.ChainExecutorObserver;
import com.github.nishgpt.chainexecutor.models.observability.config.ChainExecutorObservationConfig;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.ObservationSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.ObservationSinkConfigurationVisitor;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.SinkType;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.CustomSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.LogSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.StorageSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.payload.ObservationPayload;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

/**
 * Manager class for Chain Executor Observability feature. This class is responsible for holding the current
 * observability configuration and providing access to it throughout the application. It allows for initialization and
 * refreshing of the configuration, ensuring that all components have access to the latest settings for observability
 * features.
 */
@SuppressWarnings({"rawtypes"})
@Slf4j
public class ChainExecutorObservabilityManager {

  private static final Validator validator = Validation.buildDefaultValidatorFactory()
      .getValidator();
  private static final AtomicReference<ObservabilityManagerState> observabilityManagerState = new AtomicReference<>(
      ObservabilityManagerState.empty());
  public static ObjectMapper mapper;
  private static Injector injector;

  public static void init(final ChainExecutorObservationConfig<?> config,
      final ObjectMapper mapper,
      final Injector injector) {
    Preconditions.checkNotNull(mapper, "ObjectMapper cannot be null");
    Preconditions.checkNotNull(injector, "Injector cannot be null");

    ChainExecutorObservabilityManager.mapper = mapper;
    ChainExecutorObservabilityManager.injector = injector;
    validate(config);
    log.info("Init:: Observability config validated successfully, applying the config...");
    applyConfig(config);
    log.info("Init:: Observability config applied successfully, observability is now active.");
  }

  public static void refreshConfig(final ChainExecutorObservationConfig<?> config) {
    validate(config);
    log.info("Refresh:: Observability config validated successfully, applying the config...");
    applyConfig(config);
    log.info("Refresh:: Observability config applied successfully, observability is now active.");
  }

  protected static ChainExecutorObservationConfig<?> getObservationConfig() {
    final var observationConfig = observabilityManagerState.get()
        .config();
    //should not be needed but adding as a fallback
    return Objects.nonNull(observationConfig)
        ? observationConfig
        : ChainExecutorObservationConfig.builder()
            .build();
  }

  protected static void dispatch(final ObservationPayload payload) {
    final var state = observabilityManagerState.get();
    state.sinks()
        .forEach(sink -> state.executorService()
            .submit(() -> sink.consume(payload)));
  }

  private static void applyConfig(final ChainExecutorObservationConfig<?> config) {
    ObservabilityManagerState oldState;
    //check if the new config is completely disabling the observability features.
    if (!config.isEnabled()) {
      oldState = observabilityManagerState.getAndSet(ObservabilityManagerState.empty());
    } else {
      //Should apply the config to all the relevant components, e.g. initialize sinks, set up threadpools etc.
      final var sinks = buildSinks(config.getEnabledSinks());
      final var executorService = Executors.newFixedThreadPool(config.getObservationThreadpoolSize());

      //replace the old state with the new state atomically
      oldState = observabilityManagerState.getAndSet(
          new ObservabilityManagerState(config, sinks, executorService));
    }

    //shutdown the old executor service if it exists
    if (Objects.nonNull(oldState.executorService())) {
      oldState.executorService()
          .shutdown();
    }
  }

  private static void validate(final ChainExecutorObservationConfig<?> config) {
    //basic bean validations
    final var violations = validator.validate(config);
    if (!violations.isEmpty()) {
      final var message = violations.stream()
          .map(v -> String.join(" ", v.getPropertyPath()
              .toString(), v.getMessage()))
          .collect(Collectors.joining(", ",
              String.format("Validations errors for %s :", ChainExecutorObservationConfig.class.getName()),
              "."));
      throw ChainExecutorException.error(ErrorCode.CONFIG_VALIDATION_ERROR, message);
    }

    //custom validations
    if (config.isEnabled() && config.getEnabledSinks()
        .isEmpty()) {
      throw ChainExecutorException.error(ErrorCode.CONFIG_VALIDATION_ERROR,
          "Provide at least one sink configuration when observability is enabled");
    }

    final var sinks = config.getEnabledSinks()
        .stream()
        .collect(Collectors.groupingBy(
            ObservationSinkConfiguration::getSinkType));

    if (config.isEnabled() && sinks.containsKey(SinkType.CUSTOM)) {
      final var customSinkConfiguration = (CustomSinkConfiguration) sinks.get(SinkType.CUSTOM)
          .get(0);
      if (getCustomObserverClasses(customSinkConfiguration).size() != 1) {
        throw ChainExecutorException.error(ErrorCode.CONFIG_VALIDATION_ERROR,
            "Exactly one class annotated with @ChainExecutorObserver should be present in the specified package for custom sink configuration");
      }
    }

  }

  private static Set<ObservationSink> buildSinks(final Set<ObservationSinkConfiguration> enabledSinks) {
    final Set<ObservationSink> newSinks = new HashSet<>();
    enabledSinks.forEach(sinkConfiguration -> sinkConfiguration.accept(new ObservationSinkConfigurationVisitor<Void>() {
      @Override
      public Void visit(LogSinkConfiguration configuration) {
        newSinks.add(new LogSink(configuration));
        return null;
      }

      @Override
      public Void visit(CustomSinkConfiguration configuration) {
        getCustomObserverClasses(configuration).stream()
            .findFirst()
            .ifPresent(annotatedClass -> {
              if (ObservationSink.class.isAssignableFrom(annotatedClass)) {
                newSinks.add((ObservationSink) injector.getInstance(annotatedClass));
              }
            });
        return null;
      }

      @Override
      public Void visit(StorageSinkConfiguration configuration) {
        //TODO:: implement StorageSink and add to newSinks
        return null;
      }
    }));

    return Collections.unmodifiableSet(newSinks);
  }

  private static Set<Class<?>> getCustomObserverClasses(final CustomSinkConfiguration customSinkConfiguration) {
    final var reflections = new Reflections(customSinkConfiguration.getObserverPackage());
    return reflections.getTypesAnnotatedWith(ChainExecutorObserver.class);
  }

}