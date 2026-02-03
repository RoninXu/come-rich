package com.finance.planner.ai.agent.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentTool {
    String name();
    String description();
    RiskLevel riskLevel() default RiskLevel.LOW;
}
