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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import java.lang.annotation.Annotation;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
@SuppressWarnings("rawtypes")
public abstract class StageExecutorFactory<A extends Annotation> {

  private final Map<StageExecutorKey, StageExecutor> executors = Maps.newConcurrentMap();

  @SuppressWarnings("unchecked")
  public StageExecutorFactory(final String executorPackage,
      final Injector injector,
      final Class<A> annotationClass) {
    Preconditions.checkNotNull(injector, "Injector cannot be null");
    final var reflections = new Reflections(executorPackage);
    final var annotatedClasses = reflections.getTypesAnnotatedWith(annotationClass);

    annotatedClasses.forEach(annotatedClass -> {
      if (StageExecutor.class.isAssignableFrom(annotatedClass)) {
        final var instance = (StageExecutor) injector.getInstance(annotatedClass);
        instance.getExecutorKeys()
            .forEach(key -> executors.put((StageExecutorKey) key, instance));
      }
    });
  }

  public StageExecutor getExecutor(StageExecutorKey key) {
    return executors.get(key);
  }
}
