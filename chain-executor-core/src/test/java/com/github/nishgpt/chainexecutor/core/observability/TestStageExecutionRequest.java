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

import com.github.nishgpt.chainexecutor.models.stage.Stage;

public class TestStageExecutionRequest extends BaseTestExecutionRequest {

  private String requestId;
  private String payload;

  public TestStageExecutionRequest(String requestId, String payload) {
    this.requestId = requestId;
    this.payload = payload;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getPayload() {
    return payload;
  }

  @Override
  public Stage getStage() {
    return TestStage.STAGE1;
  }
}
