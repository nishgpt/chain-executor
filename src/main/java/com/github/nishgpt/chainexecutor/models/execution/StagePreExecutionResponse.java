package com.github.nishgpt.chainexecutor.models.execution;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StagePreExecutionResponse <T extends ExecutionContext>{
    T context;
    StagePreExecutionStatus status;
}
