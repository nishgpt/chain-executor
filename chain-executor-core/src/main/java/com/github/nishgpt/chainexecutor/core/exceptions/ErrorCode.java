package com.github.nishgpt.chainexecutor.core.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
  INVALID_CHAIN,
  EXECUTION_ERROR,
  INVALID_EXECUTION_STAGE,
  EXECUTION_NOT_COMPLETED,
  PRE_EXECUTION_FAILED
}