package com.github.nishgpt.chainexecutor.models.execution;

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
