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

import java.util.Set;
import lombok.Getter;

public enum MethodCriticality {
  CRITICAL(Set.of(ObservationDepth.STANDARD, ObservationDepth.GRANULAR)),
  MEDIUM(Set.of(ObservationDepth.GRANULAR)),
  LOW(Set.of(ObservationDepth.GRANULAR)),
  ;

  @Getter
  private Set<ObservationDepth> eligibleDepths;

  MethodCriticality(Set<ObservationDepth> eligibleDepths) {
    this.eligibleDepths = eligibleDepths;
  }
}
