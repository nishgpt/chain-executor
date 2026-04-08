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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to indicate that a class is an observer for chain executor events.
 * This can be used by clients to implement custom observers that listen to the events dispatched by the ChainExecutorObservabilityManager.
 * By annotating a class with @ChainExecutorObserver, it can be automatically detected and registered as an observer in the system
 * For now, only one observer class is allowed, necessary validations will be in place to detect the same
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChainExecutorObserver {

}
