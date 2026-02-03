package com.finance.planner.ai.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ToolResult executeTool(Tool tool, Long userId, Object rawParams, BigDecimal riskThreshold) {
        try {
            Object params = objectMapper.convertValue(rawParams, tool.getParameterClass());
            Set<ConstraintViolation<Object>> violations = validator.validate(params);
            if (!violations.isEmpty()) {
                String message = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining("; "));
                return ToolResult.failure("参数校验失败: " + message);
            }
            return tool.execute(userId, params);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", e.getMessage(), e);
            return ToolResult.failure("工具执行失败: " + e.getMessage());
        }
    }

    public RiskLevel resolveRiskLevel(Tool tool, Object rawParams, BigDecimal riskThreshold) {
        if (tool instanceof RiskAwareTool<?> riskAwareTool) {
            try {
                Object params = objectMapper.convertValue(rawParams, tool.getParameterClass());
                @SuppressWarnings("unchecked")
                RiskLevel level = ((RiskAwareTool<Object>) riskAwareTool).evaluateRisk(params, riskThreshold);
                if (level != null) {
                    return level;
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate dynamic risk for tool {}: {}", tool.getName(), e.getMessage());
            }
        }
        return tool.getRiskLevel();
    }
}
