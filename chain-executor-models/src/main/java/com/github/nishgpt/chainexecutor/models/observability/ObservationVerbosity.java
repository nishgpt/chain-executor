package com.github.nishgpt.chainexecutor.models.observability;

public enum ObservationVerbosity {
  //Will capture basic details only
  MINIMAL,
  //Will capture additional context of request, response etc.
  VERBOSE,
  ;
}
