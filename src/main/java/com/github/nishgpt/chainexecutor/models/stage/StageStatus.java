package com.github.nishgpt.chainexecutor.models.stage;

import lombok.Getter;

@Getter
public enum StageStatus {
  NOT_INITIATED,
  INITIATED,
  COMPLETED,
  FAILED,
  ERRORRED;

  public boolean isTerminal() {
    return isFailed() || isCompleted();
  }

  public boolean isFailed() {
    return FAILED.equals(this);
  }

  public boolean isCompleted() {
    return COMPLETED.equals(this);
  }

  public boolean isPending() {
    return INITIATED.equals(this);
  }

  public boolean isNotInitiated() {
    return NOT_INITIATED.equals(this);
  }

}
