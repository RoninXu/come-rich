package com.finance.planner.ai.agent.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AgentExecutionContext {

    private String sessionId;
    private Long userId;
    private int iteration;
    private boolean waitingForConfirmation;
    private List<Map<String, Object>> messages = new ArrayList<>();
}
