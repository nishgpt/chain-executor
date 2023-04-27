package com.github.nishgpt.chainexecutor.models.stage;

import java.util.Set;
import lombok.Getter;

@Getter
public enum StageStatus {
  NOT_INITIATED,
  INITIATED,
  COMPLETED,
  SKIPPED,
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

  public boolean isSkipped() {
    return SKIPPED.equals(this);
  }

  public boolean isExecutable() {
    return Set.of(NOT_INITIATED, INITIATED, ERRORRED).contains(this);
  }

  public boolean isCompletedOrSkipped() {
    return Set.of(COMPLETED, SKIPPED).contains(this);
  }
}
