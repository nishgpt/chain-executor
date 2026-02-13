package com.github.nishgpt.chainexecutor.models.execution;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StagePreExecuteResponse<T extends ExecutionContext> {

  private T context;
  private StagePreExecuteStatus status;
}
