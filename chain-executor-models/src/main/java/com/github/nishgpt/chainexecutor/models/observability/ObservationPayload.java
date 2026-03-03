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
package com.github.nishgpt.chainexecutor.models.observability;

import com.github.nishgpt.chainexecutor.models.execution.ExecutionContext;
import com.github.nishgpt.chainexecutor.models.execution.ExecutorAuxiliaryKey;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutionRequest;
import com.github.nishgpt.chainexecutor.models.stage.Stage;
import com.github.nishgpt.chainexecutor.models.stage.StageChainIdentifier;
import com.github.nishgpt.chainexecutor.models.stage.StageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
//TODO:: see if need to break into after and before separate payload classes
public class ObservationPayload {

  private String observationId; //unique identifier for the observation
  private ObservationPhase phase;
  private String methodName;
  //TODO:: method execution status - SUCCESS, FAILURE - for after invocation phase.
  private Stage stage;
  private StageStatus stageStatus;
  private Exception exception;
  private ExecutionContext context;
  private ExecutorAuxiliaryKey auxiliaryKey;
  private StageChainIdentifier chainIdentifier;
  private StageExecutionRequest request;
  private long timestampInMillis;
}
