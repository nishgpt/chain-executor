package com.github.nishgpt.chainexecutor.core.models.stage;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StageChain<T extends Stage> {

  private T head;
  private Map<T, T> forwardChainMappings;
}
