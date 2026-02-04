package com.finance.planner.ai.agent.tool;

public interface Tool {

    String getName();

    String getDescription();

    RiskLevel getRiskLevel();

    Class<?> getParameterClass();

    ToolResult execute(Long userId, Object params);
}
