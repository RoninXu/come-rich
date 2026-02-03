package com.finance.planner.ai.agent.dto;

import com.finance.planner.ai.agent.tool.ToolResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolCallPayload {

    private String toolCallId;
    private String toolName;
    private String arguments;
    private ToolResult result;
}
