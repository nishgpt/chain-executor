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

import com.github.nishgpt.chainexecutor.core.observability.sink.ObservationSink;
import com.github.nishgpt.chainexecutor.models.observability.config.ChainExecutorObservationConfig;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public record ObservabilityManagerState(ChainExecutorObservationConfig config,
                                        Set<ObservationSink> sinks,
                                        ExecutorService executorService) {

  static ObservabilityManagerState empty() {
    return new ObservabilityManagerState(ChainExecutorObservationConfig.builder()
        .build(), Set.of(), null);
  }
}
