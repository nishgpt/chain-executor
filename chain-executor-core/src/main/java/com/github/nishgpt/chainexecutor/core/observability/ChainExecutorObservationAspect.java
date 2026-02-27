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

import com.github.nishgpt.chainexecutor.models.execution.StageExecutorKey;
import com.github.nishgpt.chainexecutor.models.stage.Stage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ChainExecutorObservationAspect {

  @Pointcut("execution(* com.github.nishgpt.chainexecutor.core.execution.*(..))")
  public void executionManagerMethodCalled() {
  }

  @Pointcut("@annotation(com.github.nishgpt.chainexecutor.models.observability.ObservedMethod)")
  public void observedMethodCalled() {
  }

  @Around("executionManagerMethodCalled() && observedMethodCalled()")
  public Object aroundExecutionManagerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    // You can use the method name or any other relevant information for the observation
    final var methodParams = joinPoint.getArgs();
    final var stage = extractStage(methodParams);
    final var config = ChainExecutorObservabilityManager.getObservationConfig();

    //If observation config is not enabled for the stage, then proceed without observation
    if (!config.getObservationConfig(stage)
        .isEnabled()) {
      return joinPoint.proceed();
    }
    //prepare an empty list of observation payloads
    //prepare observation payload before method execution
    try {
      final Object response = joinPoint.proceed();
      //augment response into observation payload
      return response;
    } catch (Throwable t) {
      //augment exception into observation payload and rethrow
      throw t;
    } finally {
      //for each item in observation payload list call a dispatcher to publish the observation.
    }
  }


  //TODO:: functionality to extract a field from parent observation payload if the stage is not present in method params
  private Stage extractStage(final Object[] methodParams) {
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


  //TODO:: check if the object needs to be snapshotted / deep copied or as is.
  @SuppressWarnings("unchecked")
  private <T> T extractField(final Object[] methodParams,
      final Class<T> fieldClass) {
    for (Object methodParam : methodParams) {
      if (fieldClass.isAssignableFrom(methodParam.getClass())) {
        return (T) methodParam;
      }
    }
    return null;
  }


}
