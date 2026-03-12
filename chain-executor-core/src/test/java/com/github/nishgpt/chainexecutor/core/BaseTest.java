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
package com.github.nishgpt.chainexecutor.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nishgpt.chainexecutor.core.observability.TestExecutionContext;
import com.github.nishgpt.chainexecutor.core.observability.TestStage;
import com.github.nishgpt.chainexecutor.core.observability.TestStageExecutionRequest;
import java.util.Map;

public class BaseTest {

  protected static final TestStageExecutionRequest REQUEST = TestStageExecutionRequest.builder()
      .requestId("REQ123")
      .payload("Test Payload")
      .build();

  protected static final TestExecutionContext EXECUTION_CONTEXT = TestExecutionContext.builder()
      .id("APP123")
      .applicationId("APP123")
      .applicationStatus("IN_PROGRESS")
      .stageStatus(
          Map.of(TestStage.STAGE1, "COMPLETED", TestStage.STAGE2, "IN_PROGRESS", TestStage.STAGE3, "NOT_INITIATED"))
      .build();

  protected static final ObjectMapper MAPPER = new ObjectMapper();
}
