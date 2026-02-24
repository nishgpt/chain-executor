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

import java.util.Objects;

/**
 * Manager class for Chain Executor Observability feature. This class is responsible for holding the current
 * observability configuration and providing access to it throughout the application. It allows for initialization and
 * refreshing of the configuration, ensuring that all components have access to the latest settings for observability
 * features.
 */
public class ChainExecutorObservabilityManager {

  private static ChainExecutorObservationConfig observationConfig;

  public static void init(final ChainExecutorObservationConfig config) {
    ChainExecutorObservabilityManager.observationConfig = config;
  }

  public static void refreshConfig(final ChainExecutorObservationConfig config) {
    //currently same as init, but can be extended to have different logic for refreshing config if needed
    ChainExecutorObservabilityManager.observationConfig = config;
  }

  protected static ChainExecutorObservationConfig getObservationConfig() {
    if (Objects.isNull(observationConfig)) {
      //client has probably opted out of observability, return a default config with all features disabled
      observationConfig = ChainExecutorObservationConfig.builder()
          .enabled(false)
          .build();
    }
    return observationConfig;
  }

}
