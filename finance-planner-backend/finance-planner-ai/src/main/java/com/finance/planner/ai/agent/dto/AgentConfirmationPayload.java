package com.finance.planner.ai.agent.dto;

import com.finance.planner.ai.agent.tool.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfirmationPayload {

    private String confirmationId;
    private String toolCallId;
    private String toolName;
    private RiskLevel riskLevel;
    private String summary;
}
