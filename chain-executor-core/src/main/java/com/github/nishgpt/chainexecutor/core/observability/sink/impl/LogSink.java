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
package com.github.nishgpt.chainexecutor.core.observability.sink.impl;

import com.github.nishgpt.chainexecutor.core.observability.sink.ObservationSink;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.LogLevel.Visitor;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.LogSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.payload.ObservationPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LogSink implements ObservationSink {

  private static final String LOG_MESSAGE = "Observation consumed {}";
  private final LogSinkConfiguration configuration;

  @Override
  public void consume(final ObservationPayload payload) {
    configuration.getLogLevel()
        .accept(new Visitor<Void>() {
          @Override
          public Void visitTrace() {
            log.trace(LOG_MESSAGE, payload);
            return null;
          }

          @Override
          public Void visitDebug() {
            log.debug(LOG_MESSAGE, payload);
            return null;
          }

          @Override
          public Void visitInfo() {
            log.info(LOG_MESSAGE, payload);
            return null;
          }
        });
  }
}
