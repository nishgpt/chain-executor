package com.github.nishgpt.chainexecutor.core.models.execution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
public class ExecutionContext {

  private final String id;
}

