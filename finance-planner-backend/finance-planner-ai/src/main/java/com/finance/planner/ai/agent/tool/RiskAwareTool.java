package com.finance.planner.ai.agent.tool;

import java.math.BigDecimal;

public interface RiskAwareTool<T> {

    RiskLevel evaluateRisk(T params, BigDecimal threshold);
}
