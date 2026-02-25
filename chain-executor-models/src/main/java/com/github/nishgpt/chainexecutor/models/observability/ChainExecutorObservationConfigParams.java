package com.github.nishgpt.chainexecutor.models.observability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ChainExecutorObservationConfigParams {

  private boolean enabled;
  private ObservationDepth depth;
  private ObservationVerbosity verbosity;
}
