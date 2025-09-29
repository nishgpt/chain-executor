package com.github.nishgpt.chainexecutor.models.execution;

public enum StagePreExecuteStatus {
    PASS, BLOCK, FAIL;

    public boolean isPass() {
        return PASS.equals(this);
    }

    public boolean isBlocked() {
        return BLOCK.equals(this);
    }

    public boolean isFailed() {
        return FAIL.equals(this);
    }
}
