package com.github.nishgpt.chainexecutor.models.execution;

import com.github.nishgpt.chainexecutor.models.stage.Stage;
import com.github.nishgpt.chainexecutor.models.stage.StageStatus;
import java.util.Set;

public interface StageExecutor<M extends Stage, K extends ExecutorAuxiliaryKey, T extends ExecutionContext, U extends StageExecutionRequest> {

  /**
   * Returns set of identifiers for which this can be executed
   *
   * @return
   */
  Set<StageExecutorKey<M, K>> getExecutorKeys();

  /**
   * Checks and updates the stage status to INITIATED
   */
  T init(T context);

  /**
   * Executes this stage
   *
   * @param context
   * @param request
   * @return
   */
  T execute(T context, U request);

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
   * Gets stage status
   *
   * @param context
   * @return
   */
  StageStatus getStageStatus(T context);
}
