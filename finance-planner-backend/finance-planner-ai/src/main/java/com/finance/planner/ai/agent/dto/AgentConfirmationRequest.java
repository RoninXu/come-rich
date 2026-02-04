package com.finance.planner.ai.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgentConfirmationRequest {

    @NotBlank
    private String confirmationId;

    @NotNull
    private Boolean accepted;
}
