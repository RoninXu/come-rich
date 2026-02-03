package com.finance.planner.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmStreamEvent {

    private String content;
    private List<ToolCallChunk> toolCalls;
    private String finishReason;
}
