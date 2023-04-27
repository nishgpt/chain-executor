package com.github.nishgpt.chainexecutor;

import com.github.nishgpt.chainexecutor.exceptions.ChainExecutorException;
import com.github.nishgpt.chainexecutor.exceptions.ErrorCode;
import com.github.nishgpt.chainexecutor.models.execution.ExecutionContext;
import com.github.nishgpt.chainexecutor.models.execution.ExecutorAuxiliaryKey;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutionRequest;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutor;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutorFactory;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutorKey;
import com.github.nishgpt.chainexecutor.models.stage.Stage;
import com.github.nishgpt.chainexecutor.models.stage.StageChainIdentifier;
import com.github.nishgpt.chainexecutor.models.stage.StageChainRegistry;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@SuperBuilder
@SuppressWarnings("rawtypes")
public abstract class StageExecutionManager<T extends Stage, U extends ExecutionContext, V extends StageExecutionRequest, K extends ExecutorAuxiliaryKey, C extends StageChainIdentifier> {

  @Getter
  private final StageChainRegistry<T, C> chainRegistry;
  private final StageExecutorFactory executorFactory;

  @SuppressWarnings("unchecked")
  public U execute(StageExecutorKey<T, K> stageExecutorKey, C chainIdentifier, U context,
      V request) throws ChainExecutorException {
    try {
      validatePreviousStagesCompletion(chainIdentifier, context, stageExecutorKey.getAuxiliaryKey(),
          stageExecutorKey.getStage());

      //Check if first executable stage is same as requested
      if (!stageExecutorKey.getStage().equals(getFirstNonCompletedStage(chainIdentifier, context,
          stageExecutorKey.getAuxiliaryKey()))) {
        throw ChainExecutorException.error(ErrorCode.INVALID_EXECUTION_STAGE);
      }

      final var executor = executorFactory.getExecutor(stageExecutorKey);
      final var currentStatus = executor.getStageStatus(context);

      //Init this stage if not initiated and can't be skipped
      if (currentStatus.isNotInitiated()) {
        final var updatedContext = (U) executor.skipIfApplicable(context);
        if(executor.getStageStatus(updatedContext).isSkipped()) {
          context = updatedContext;
          log.info("Skipped {} Stage for id - {}", stageExecutorKey.getStage(), context.getId());
        } else {
          log.info("Initiating {} Stage for id - {}", stageExecutorKey.getStage(), context.getId());
          context = (U) executor.init(context);
        }
      }

      //If the stage is already Failed, break execution here
      if (currentStatus.isFailed()) {
        return context;
      }

      if (currentStatus.isExecutable()) {
        //Execute this stage
        log.info("Executing {} Stage for id - {}", stageExecutorKey.getStage(), context.getId());
        context = (U) executor.execute(context, request);
      }

      //If stage has not been processed, expect a call to resume flow
      //Otherwise, perform post completion actions
      if (executor.getStageStatus(context).isProcessedSuccessfully()) {
        return performPostCompletionSteps(context, executor, stageExecutorKey,
            chainIdentifier);
      }
      return context;
    } catch (ChainExecutorException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error executing stage {}", stageExecutorKey.getStage());
      throw ChainExecutorException.propagate(ErrorCode.EXECUTION_ERROR, e);
    }
  }

  @SuppressWarnings("unchecked")
  public U resume(StageExecutorKey<T, K> stageExecutorKey, C chainIdentifier, U context)
      throws ChainExecutorException {
    try {
      final var executor = executorFactory.getExecutor(stageExecutorKey);

      final var currentStatus = executor.getStageStatus(context);
      if (!currentStatus.isPending()) {
        log.info("Cannot resume stage {}, current status {}", stageExecutorKey.getStage(),
            currentStatus);
        return context;
      }

      //Resume this stage if pending
      var updatedContext = (U) executor.resume(context);

      if (executor.getStageStatus(updatedContext).isCompleted()) {
        return performPostCompletionSteps(updatedContext, executor, stageExecutorKey,
            chainIdentifier);
      }

      return updatedContext;
    } catch (ChainExecutorException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error executing stage {}", stageExecutorKey.getStage());
      throw ChainExecutorException.propagate(ErrorCode.EXECUTION_ERROR, e);
    }
  }

  protected abstract U finishExecution(U context);

  @SuppressWarnings("unchecked")
  private U performPostCompletionSteps(U context, StageExecutor executor,
      StageExecutorKey<T, K> stageExecutorKey, C chainIdentifier) {

    //Post execution processing, if any
    final var updatedContext = (U) executor.postExecution(context);

    //check for further stages and init the first initable stage
    var nextStage = chainRegistry.getNextStageChain(chainIdentifier,
        stageExecutorKey.getStage());

    while (Objects.nonNull(nextStage)) {
      final var nextExecutor = getExecutor(nextStage, stageExecutorKey.getAuxiliaryKey());

      //validate status and check completion
      nextExecutor.validateStatus(updatedContext);

      //check if stage can be skipped
      if (nextExecutor.getStageStatus(updatedContext).isNotInitiated()) {
        nextExecutor.skipIfApplicable(updatedContext);
      }

      if (nextExecutor.getStageStatus(updatedContext).isExecutable()) {
        break;
      }
      nextStage = chainRegistry.getNextStageChain(chainIdentifier, nextStage);
    }

    // if last stage has completed: revisit for a more concrete check
    if (Objects.isNull(nextStage)) {
      return finishExecution(context);
    }

    //If stage is not initiated, call init
    if (getExecutor(nextStage, stageExecutorKey.getAuxiliaryKey())
        .getStageStatus(updatedContext).isNotInitiated()) {
      log.info("Initiating {} Stage for id - {}", nextStage, context.getId());
      return (U) getExecutor(nextStage, stageExecutorKey.getAuxiliaryKey()).init(updatedContext);
    }

    return updatedContext;
  }

  @SuppressWarnings("unchecked")
  public T getFirstNonCompletedStage(C chainIdentifier, U context, K auxiliaryKey) {
    var currentStage = chainRegistry.getChainHead(chainIdentifier);
    do {
      final var executor = getExecutor(currentStage, auxiliaryKey);
      if (!executor.getStageStatus(context).isCompleted()) {
        break;
      }
      currentStage = chainRegistry.getNextStageChain(chainIdentifier, currentStage);
    } while (Objects.nonNull(currentStage));

    return currentStage;
  }

  protected StageExecutor getExecutor(T stage, K auxiliaryKey) {
    final var executorKey = StageExecutorKey.<T, K>builder()
        .stage(stage)
        .auxiliaryKey(auxiliaryKey)
        .build();
    return executorFactory.getExecutor(executorKey);
  }

  @SuppressWarnings("unchecked")
  protected void validatePreviousStagesCompletion(C chainIdentifier, U context, K auxiliaryKey,
      T endStage) {
    var currentStage = chainRegistry.getChainHead(chainIdentifier);
    while (Objects.nonNull(currentStage) && !currentStage.equals(endStage)) {
      final var executor = getExecutor(currentStage, auxiliaryKey);
      executor.validateStatus(context);

      //break if stage is not completed
      if (!executor.getStageStatus(context).isCompleted()) {
        break;
      }
      currentStage = chainRegistry.getNextStageChain(chainIdentifier, currentStage);
    }
  }
}
