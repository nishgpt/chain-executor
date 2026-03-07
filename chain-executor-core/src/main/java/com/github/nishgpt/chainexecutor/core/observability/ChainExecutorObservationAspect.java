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

import static com.github.nishgpt.chainexecutor.core.observability.ChainExecutorObservabilityManager.dispatch;
import static com.github.nishgpt.chainexecutor.core.observability.ObservationPayloadBuilder.extractExecutorFactory;
import static com.github.nishgpt.chainexecutor.core.observability.ObservationPayloadBuilder.extractStage;
import static com.github.nishgpt.chainexecutor.core.observability.ObservationPayloadBuilder.prepareAfterPayload;
import static com.github.nishgpt.chainexecutor.core.observability.ObservationPayloadBuilder.prepareBeforePayload;

import com.github.nishgpt.chainexecutor.models.observability.MethodCriticality;
import com.github.nishgpt.chainexecutor.models.observability.ObservedMethod;
import com.github.nishgpt.chainexecutor.models.stage.Stage;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class ChainExecutorObservationAspect {

  private static final ThreadLocal<String> GROUPING_ID = new ThreadLocal<>();

  @Pointcut("execution(* com.github.nishgpt.chainexecutor.core.execution.StageExecutionManager.*(..))")
  public void executionManagerMethodCalled() {
  }

  @Around("executionManagerMethodCalled() && @annotation(observedMethod)")
  public Object aroundExecutionManagerMethod(ProceedingJoinPoint joinPoint,
      ObservedMethod observedMethod) throws Throwable {
    // You can use the method name or any other relevant information for the observation
    final var stage = extractStage(joinPoint.getArgs());

    //check if we can skip observation
    if (skipObservation(stage, observedMethod.criticality())) {
      return joinPoint.proceed();
    }

    //outermost invocation will be responsible for creating and managing observation group id which will be used to correlate all the observations for a particular execution flow.
    final var isRootInvocation = Objects.isNull(GROUPING_ID.get());
    final String observationGroupId = getObservationGroupId(isRootInvocation);
    final var executorFactory = extractExecutorFactory(joinPoint);

    //dispatch before payload
    dispatch(prepareBeforePayload(stage, joinPoint, observationGroupId, executorFactory));

    try {
      //proceed with method execution.
      final Object response = joinPoint.proceed();

      //dispatch after payload for successful execution
      dispatch(prepareAfterPayload(stage, joinPoint, observationGroupId, response, null, executorFactory));
      return response;
    } catch (Throwable t) {
      //dispatch after payload for non-successful execution
      dispatch(prepareAfterPayload(stage, joinPoint, observationGroupId, null, t, executorFactory));
      throw t;
    } finally {
      if (isRootInvocation) {
        GROUPING_ID.remove();
      }
    }
  }

  private String getObservationGroupId(boolean isRootInvocation) {
    if (isRootInvocation) {
      GROUPING_ID.set(UUID.randomUUID()
          .toString());
    }
    return GROUPING_ID.get(); //TODO:: check if we need to move to better/compact id convention
  }

  private boolean skipObservation(final Stage stage,
      final MethodCriticality methodCriticality) {
    if (Objects.isNull(stage)) {
      return true;
    }

    final var stageConfigParams = ChainExecutorObservabilityManager.getObservationConfig()
        .getConfigParams(stage);

    //if stage observability is disabled or depth is not eligible
    return !stageConfigParams.isEnabled() || !methodCriticality.getEligibleDepths()
        .contains(stageConfigParams.getDepth());
  }
}
