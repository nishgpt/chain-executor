package com.github.nishgpt.chainexecutor.models.execution;

import com.github.nishgpt.chainexecutor.models.stage.Stage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StageExecutorKey<T extends Stage, K extends ExecutorAuxiliaryKey> {

  private T stage;
  private K auxiliaryKey;
}
