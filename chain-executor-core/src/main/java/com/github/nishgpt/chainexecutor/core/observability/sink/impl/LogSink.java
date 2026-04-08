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

import com.github.nishgpt.chainexecutor.core.observability.ChainExecutorObservabilityManager;
import com.github.nishgpt.chainexecutor.core.observability.sink.ObservationSink;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.LogLevel.Visitor;
import com.github.nishgpt.chainexecutor.models.observability.config.sink.impl.LogSinkConfiguration;
import com.github.nishgpt.chainexecutor.models.observability.payload.ObservationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSink implements ObservationSink {

    private static final Logger log = LoggerFactory.getLogger(LogSink.class.getName());
    private static final String LOG_MESSAGE = "Observation consumed {}";
    private final LogSinkConfiguration configuration;

    public LogSink(final LogSinkConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void consume(final ObservationPayload payload) {
        try {
            final var serPayload = ChainExecutorObservabilityManager.mapper.writeValueAsString(payload);
            configuration.getLogLevel()
                    .accept(new Visitor<Void>() {
                        @Override
                        public Void visitTrace() {
                            log.trace(LOG_MESSAGE, serPayload);
                            return null;
                        }

                        @Override
                        public Void visitDebug() {
                            log.debug(LOG_MESSAGE, serPayload);
                            return null;
                        }

                        @Override
                        public Void visitInfo() {
                            log.info(LOG_MESSAGE, serPayload);
                            return null;
                        }
                    });
        } catch (Exception e) {
            log.warn("Error logging observation payload, error: {}", e.getMessage());
        }
    }
}
