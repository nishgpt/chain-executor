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
package com.github.nishgpt.chainexecutor.core.models.stage;

import java.util.Set;
import lombok.Getter;

@Getter
public enum StageStatus {
  NOT_INITIATED,
  INITIATED,
  COMPLETED,
  SKIPPED,
  FAILED,
  ERRORRED;

  public boolean isTerminal() {
    return isFailed() || isCompleted();
  }

  public boolean isFailed() {
    return FAILED.equals(this);
  }

  public boolean isCompleted() {
    return COMPLETED.equals(this);
  }

  public boolean isPending() {
    return INITIATED.equals(this);
  }

  public boolean isNotInitiated() {
    return NOT_INITIATED.equals(this);
  }

  public boolean isSkipped() {
    return SKIPPED.equals(this);
  }

  public boolean isExecutable() {
    return Set.of(NOT_INITIATED, INITIATED, ERRORRED)
        .contains(this);
  }

  public boolean isCompletedOrSkipped() {
    return Set.of(COMPLETED, SKIPPED)
        .contains(this);
  }
}
