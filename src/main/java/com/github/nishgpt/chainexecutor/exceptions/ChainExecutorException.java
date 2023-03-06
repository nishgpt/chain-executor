package com.github.nishgpt.chainexecutor.exceptions;

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
  public ChainExecutorException(final ErrorCode errorCode, final String message,
      final Throwable cause) {
    super(message, cause);

    this.errorCode = errorCode;
  }

  public static ChainExecutorException error(final ErrorCode errorCode) {
    return new ChainExecutorException(errorCode, ERROR_OCCURRED_MESSAGE, null);
  }

  public static ChainExecutorException propagate(final ErrorCode errorCode, final Throwable cause) {
    if (cause instanceof ChainExecutorException) {
      return (ChainExecutorException) cause;
    }
    return new ChainExecutorException(errorCode, ERROR_OCCURRED_MESSAGE, cause);
  }

}
