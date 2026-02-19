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
package com.github.nishgpt.chainexecutor.core.models.execution;

import com.github.nishgpt.chainexecutor.core.models.stage.Stage;
import com.github.nishgpt.chainexecutor.core.models.stage.StageStatus;
import java.util.Set;

public interface StageExecutor<M extends Stage, K extends ExecutorAuxiliaryKey, T extends ExecutionContext, U extends StageExecutionRequest> {

  /**
   * Returns set of identifiers for which this can be executed
   *
   * @return
   */
  Set<StageExecutorKey<M, K>> getExecutorKeys();

  /**
   * Checks and updates the stage status to INITIATED. This should be used for things which require only one time
   * initiation.
   */
  T init(T context);

  /**
   * This function should be used to fetch time sensitive info required to be used for execute
   */
  T fetchInfo(T context);

  /**
   * This function should be used to enrich the context before execution  and run some pre execution validations
   *
   * @param context current execution context
   * @return StagePreExecuteResponse with updated context and status
   */
  default StagePreExecuteResponse<T> preExecute(T context) {
    return StagePreExecuteResponse.<T>builder()
        .context(context)
        .status(StagePreExecuteStatus.PASS)
        .build();
  }

  /**
   * Executes this stage
   *
   * @param context
   * @param request In case of background stage, request can be null and should not be used in execution
   * @return
   */
  T execute(T context,
      U request);

  /**
   * Resumes this stage if not completed
   *
   * @param context
   * @return
   */
  T resume(T context);

  /**
   * Does any post execution activity, if applicable
   *
   * @param context
   * @return
   */
  T postExecution(T context);

  /**
   * Checks and updates the correct stage status
   */
  void validateStatus(T context);

  /**
   * Checks if the stage can be skipped
   *
   * @return
   */
  default T skipIfApplicable(T context) {
    return context;
  }

  /**
   * Gets stage status
   *
   * @param context
   * @return
   */
  StageStatus getStageStatus(T context);

  /**
   * Checks if the stage is background
   *
   * @param context
   * @return
   */
  default boolean isBackground(T context) {
    return false;
  }
}
