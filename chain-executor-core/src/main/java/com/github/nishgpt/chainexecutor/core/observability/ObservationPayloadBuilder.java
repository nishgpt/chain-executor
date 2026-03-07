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

import static com.github.nishgpt.chainexecutor.core.observability.ChainExecutorObservabilityManager.mapper;

import com.github.nishgpt.chainexecutor.core.execution.StageExecutionManager;
import com.github.nishgpt.chainexecutor.core.execution.StageExecutorFactory;
import com.github.nishgpt.chainexecutor.models.execution.ExecutionContext;
import com.github.nishgpt.chainexecutor.models.execution.ExecutorAuxiliaryKey;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutionRequest;
import com.github.nishgpt.chainexecutor.models.execution.StageExecutorKey;
import com.github.nishgpt.chainexecutor.models.observability.MethodExecutionOutcome;
import com.github.nishgpt.chainexecutor.models.observability.ObservationPhase;
import com.github.nishgpt.chainexecutor.models.observability.ObservationVerbosity.Visitor;
import com.github.nishgpt.chainexecutor.models.observability.payload.ObservationPayload;
import com.github.nishgpt.chainexecutor.models.observability.payload.impl.AfterMethodInvocationPayload;
import com.github.nishgpt.chainexecutor.models.observability.payload.impl.BeforeMethodInvocationPayload;
import com.github.nishgpt.chainexecutor.models.stage.Stage;
import com.github.nishgpt.chainexecutor.models.stage.StageChainIdentifier;
import com.github.nishgpt.chainexecutor.models.stage.StageStatus;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

@Slf4j
public class ObservationPayloadBuilder {

  private static Field EXECUTOR_FACTORY_FIELD;

  static {
    try {
      EXECUTOR_FACTORY_FIELD = StageExecutionManager.class.getDeclaredField("executorFactory");
      EXECUTOR_FACTORY_FIELD.setAccessible(true);
    } catch (NoSuchFieldException e) {
      //should not happen ideally
      log.warn("No executorFactory field found in StageExecutionManager. Observability will not work as expected.", e);
    }
  }

  private static <T> T deepCopy(final Object data,
      final Class<T> targetClass) {
    try {
      byte[] serialized = mapper.writeValueAsBytes(data);
      return mapper.readValue(serialized, targetClass);
    } catch (Exception e) {
      log.warn("Failed to deep copy object of type {}, returning null", targetClass.getName(), e);
      return null;
    }
  }

  @SuppressWarnings("rawtypes")
  static ObservationPayload prepareBeforePayload(final Stage stage,
      final JoinPoint joinPoint,
      final String observationGroupId,
      final StageExecutorFactory executorFactory) {
    final var verbosity = ChainExecutorObservabilityManager.getObservationConfig()
        .getConfigParams(stage)
        .getVerbosity();
    final var methodParams = joinPoint.getArgs();
    final var executionContext = extractField(methodParams, ExecutionContext.class, true);
    final var executorKey = extractField(methodParams, StageExecutorKey.class, false);

    //common builder for both verbosity levels
    final var payloadBuilder = BeforeMethodInvocationPayload.builder()
        .phase(ObservationPhase.BEFORE_METHOD_INVOCATION)
        .observationId(UUID.randomUUID()
            .toString())
        .observationGroupId(observationGroupId)
        .methodName(joinPoint.getSignature()
            .getName())
        .stage(stage)
        .stageStatus(extractStageStatus(executorKey, executorFactory, executionContext))
        .timestampInMillis(System.currentTimeMillis());

    return verbosity.accept(new Visitor<>() {
      @Override
      public ObservationPayload visitBasic() {
        return payloadBuilder.build();
      }

      @Override
      public ObservationPayload visitVerbose() {
        return payloadBuilder
            .context(executionContext)
            .auxiliaryKey(extractAuxiliaryKey(methodParams, executorKey))
            .chainIdentifier(extractField(methodParams, StageChainIdentifier.class, false))
            .request(extractField(methodParams, StageExecutionRequest.class, false))
            .build();
      }
    });

  }

  @SuppressWarnings("rawtypes")
  static ObservationPayload prepareAfterPayload(final Stage stage,
      final JoinPoint joinPoint,
      final String observationGroupId,
      final Object methodResponse,
      final Throwable methodException,
      final StageExecutorFactory executorFactory) {
    final var verbosity = ChainExecutorObservabilityManager.getObservationConfig()
        .getConfigParams(stage)
        .getVerbosity();
    final var methodParams = joinPoint.getArgs();
    final var executionContext = extractField(methodResponse, ExecutionContext.class, true);
    final var executorKey = extractField(methodParams, StageExecutorKey.class, false);

    //common builder for both verbosity levels
    final var payloadBuilder = AfterMethodInvocationPayload.builder()
        .phase(ObservationPhase.AFTER_METHOD_INVOCATION)
        .observationId(UUID.randomUUID()
            .toString()) //TODO:: check if we need to move to better/compact id convention
        .observationGroupId(observationGroupId)
        .methodName(joinPoint.getSignature()
            .getName())
        .stage(stage)
        .outcome(Objects.isNull(methodException)
            ? MethodExecutionOutcome.SUCCESS
            : MethodExecutionOutcome.FAILURE)
        .stageStatus(extractStageStatus(executorKey, executorFactory, executionContext))
        .timestampInMillis(System.currentTimeMillis());

    return verbosity.accept(new Visitor<>() {
      @Override
      public ObservationPayload visitBasic() {
        return payloadBuilder.build();
      }

      @Override
      public ObservationPayload visitVerbose() {
        return payloadBuilder
            .context(executionContext)
            .exception((Exception) methodException)
            .build();
      }
    });
  }

  @SuppressWarnings("rawtypes")
  static ExecutorAuxiliaryKey extractAuxiliaryKey(final Object[] methodParams,
      final StageExecutorKey executorKey) {
    //try finding using direct method params
    var auxiliaryKey = extractField(methodParams, ExecutorAuxiliaryKey.class, false);

    //if null, try to extract from executorKey
    if (Objects.isNull(auxiliaryKey) && Objects.nonNull(executorKey)) {
      auxiliaryKey = executorKey.getAuxiliaryKey();
    }

    return auxiliaryKey;
  }

  @SuppressWarnings("rawtypes")
  static StageExecutorFactory extractExecutorFactory(final JoinPoint joinPoint) {
    try {
      //assuming target is always stage execution manager
      return (StageExecutorFactory) EXECUTOR_FACTORY_FIELD.get(joinPoint.getTarget());
    } catch (IllegalAccessException e) {
      log.warn("Unable to extract executor factory from execution manager for observation", e);
      return null;
    }
  }

  static Stage extractStage(final Object[] methodParams) {
    for (Object methodParam : methodParams) {
      if (Stage.class.isAssignableFrom(methodParam.getClass())) {
        return (Stage) methodParam;
      }

      if (StageExecutorKey.class.isAssignableFrom(methodParam.getClass())) {
        return ((StageExecutorKey<? extends Stage, ?>) methodParam).getStage();
      }
    }
    return null;
  }

  static <T> T extractField(final Object[] methodParams,
      final Class<T> fieldClass,
      final boolean deepCopy) {
    for (Object methodParam : methodParams) {
      final var value = extractField(methodParam, fieldClass, deepCopy);
      if (Objects.nonNull(value)) {
        return value;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  static <T> T extractField(final Object object,
      final Class<T> fieldClass,
      final boolean deepCopy) {
    if (Objects.isNull(object)) {
      return null;
    }

    if (fieldClass.isAssignableFrom(object.getClass())) {
      return deepCopy
          ? deepCopy(object, fieldClass)
          : (T) object;
    }
    return null;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  static StageStatus extractStageStatus(StageExecutorKey executorKey,
      StageExecutorFactory executorFactory,
      ExecutionContext executionContext) {
    if (Objects.isNull(executorFactory) || Objects.isNull(executionContext) || Objects.isNull(executorKey)) {
      log.warn("Could not extract stageStatus due to missing necessary information.");
      return null;
    }

    final var executor = executorFactory.getExecutor(executorKey);
    if (Objects.isNull(executor)) {
      log.warn("Could not extract stageStatus due to null executor for executorKey: {}", executorKey);
      return null;
    }

    return executor.getStageStatus(executionContext);
  }

}
