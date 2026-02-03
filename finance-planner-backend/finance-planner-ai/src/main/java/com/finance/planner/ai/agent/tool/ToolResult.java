package com.finance.planner.ai.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    private boolean success;
    private Object data;
    private String message;
    private String error;

    public static ToolResult success(Object data) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ToolResult failure(String message) {
        return ToolResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
