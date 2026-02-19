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
package com.github.nishgpt.chainexecutor.core.exceptions;

import java.io.Serial;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChainExecutorException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1084352069557055978L;
  private static final String ERROR_OCCURRED_MESSAGE = "Error occurred";
  private final ErrorCode errorCode;

  @Builder
  public ChainExecutorException(final ErrorCode errorCode,
      final String message,
      final Throwable cause) {
    super(message, cause);

    this.errorCode = errorCode;
  }

  public static ChainExecutorException error(final ErrorCode errorCode) {
    return new ChainExecutorException(errorCode, ERROR_OCCURRED_MESSAGE, null);
  }

  public static ChainExecutorException propagate(final ErrorCode errorCode,
      final Throwable cause) {
    if (cause instanceof ChainExecutorException) {
      return (ChainExecutorException) cause;
    }
    return new ChainExecutorException(errorCode, ERROR_OCCURRED_MESSAGE, cause);
  }

}
