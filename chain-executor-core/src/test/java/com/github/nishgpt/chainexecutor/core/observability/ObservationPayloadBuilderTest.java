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

import com.github.nishgpt.chainexecutor.core.BaseTest;
import com.github.nishgpt.chainexecutor.models.execution.ExecutionContext;
import com.github.nishgpt.chainexecutor.models.observability.config.ChainExecutorObservationConfig;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class ObservationPayloadBuilderTest extends BaseTest {

  @BeforeEach
  void setup() {
    //for now basic setup
    ChainExecutorObservabilityManager.init(ChainExecutorObservationConfig.<TestStage>builder()
        .build(), MAPPER, null);
  }

  @Test
  void testExecutionContextExtraction() throws IOException {
    final var extractedContext = ObservationPayloadBuilder.extractField(EXECUTION_CONTEXT, ExecutionContext.class,
        true);
    Assertions.assertEquals(EXECUTION_CONTEXT, extractedContext);
  }
}
