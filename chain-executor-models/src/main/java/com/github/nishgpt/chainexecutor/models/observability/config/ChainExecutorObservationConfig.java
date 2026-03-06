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
package com.github.nishgpt.chainexecutor.models.observability.config;

import com.github.nishgpt.chainexecutor.models.observability.config.sink.ObservationSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.stage.Stage;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Clients can define the config as per the need. They can choose defaultConfig for global settings or stageWiseConfig
 * for stage specific settings.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ChainExecutorObservationConfig {

  @Builder.Default
  private ObservationConfigParams defaultConfigParams = ObservationConfigParams.builder()
      .build();
  @Builder.Default
  private Map<Stage, ObservationConfigParams> stageWiseConfigParams = Map.of();
  //Dedicated threadpool to be used while dispatching observation payloads to the enabled sinks.
  @Builder.Default
  private int observationThreadpoolSize = 4;
  @Valid
  @Builder.Default
  private Set<ObservationSinkConfiguration> enabledSinks = Set.of();

  public ObservationConfigParams getConfigParams(final Stage stage) {
    return stageWiseConfigParams.getOrDefault(stage, defaultConfigParams);
  }

  public boolean isEnabled() {
    return defaultConfigParams.isEnabled() || stageWiseConfigParams.values()
        .stream()
        .anyMatch(ObservationConfigParams::isEnabled);
  }
}
