package com.github.nishgpt.chainexecutor.models.observability.payload;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionInfo {

  private String errorMessage;
  private String exceptionClass;
  private String causeMessage;
  private String causeClass;

  public static ExceptionInfo from(final Throwable throwable) {
    if (Objects.isNull(throwable)) {
      return null;
    }

    return ExceptionInfo.builder()
        .errorMessage(throwable.getMessage())
        .exceptionClass(throwable.getClass()
            .getSimpleName())
        .causeMessage(Objects.nonNull(throwable.getCause())
            ? throwable.getCause()
            .getMessage()
            : null)
        .causeClass(Objects.nonNull(throwable.getCause())
            ? throwable.getCause()
            .getClass()
            .getSimpleName()
            : null)
        .build();
  }
}
