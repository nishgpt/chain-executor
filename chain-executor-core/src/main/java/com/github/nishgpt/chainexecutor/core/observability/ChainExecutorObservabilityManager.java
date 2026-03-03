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

import com.github.nishgpt.chainexecutor.core.observability.sink.ObservationSink;
import com.github.nishgpt.chainexecutor.core.observability.sink.impl.LogSink;
import com.github.nishgpt.chainexecutor.models.observability.config.ChainExecutorObservationConfig;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.ObservationSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.ObservationSinkConfigurationVisitor;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.ClientDispatchSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.LogSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.StorageSinkConfiguration;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manager class for Chain Executor Observability feature. This class is responsible for holding the current
 * observability configuration and providing access to it throughout the application. It allows for initialization and
 * refreshing of the configuration, ensuring that all components have access to the latest settings for observability
 * features.
 */
public class ChainExecutorObservabilityManager {

  private static final Validator validator = Validation.buildDefaultValidatorFactory()
      .getValidator();
  private static final AtomicReference<ObservabilityManagerState> observabilityManagerState = new AtomicReference<>(
      ObservabilityManagerState.empty());

  public static void init(final ChainExecutorObservationConfig config) {
    validate(config);
    applyConfig(config);
  }

  public static void refreshConfig(final ChainExecutorObservationConfig config) {
    validate(config);
    applyConfig(config);
  }

  protected static ChainExecutorObservationConfig getObservationConfig() {
    final var observationConfig = observabilityManagerState.get()
        .config();
    return Objects.nonNull(observationConfig)
        ? observationConfig
        : ChainExecutorObservationConfig.builder()
            .build();
  }

  private static void applyConfig(final ChainExecutorObservationConfig config) {
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

  private static void validate(final ChainExecutorObservationConfig config) {
    //TODO:: add any validation logic for the config here, e.g. if certain features are enabled, required fields must be present etc.
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
      public Void visit(ClientDispatchSinkConfiguration configuration) {
        //TODO:: implement ClientDispatchSink and add to newSinks
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

}
