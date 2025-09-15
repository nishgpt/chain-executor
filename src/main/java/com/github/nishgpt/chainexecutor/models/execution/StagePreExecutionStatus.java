package com.github.nishgpt.chainexecutor.models.execution;

public enum StagePreExecutionStatus {
    ALLOW, BLOCK, FAIL;

    public boolean isAllowed() {
        return ALLOW.equals(this);
    }

    public boolean isFailed() {
        return FAIL.equals(this);
    }
}
