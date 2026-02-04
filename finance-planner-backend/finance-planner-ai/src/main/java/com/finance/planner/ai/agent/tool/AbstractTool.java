package com.finance.planner.ai.agent.tool;

public abstract class AbstractTool<T> implements Tool {

    private final Class<T> parameterClass;
    private final AgentTool agentTool;

    protected AbstractTool(Class<T> parameterClass) {
        this.parameterClass = parameterClass;
        this.agentTool = getClass().getAnnotation(AgentTool.class);
        if (this.agentTool == null) {
            throw new IllegalStateException("AgentTool annotation is required for " + getClass().getSimpleName());
        }
    }

    @Override
    public String getName() {
        return agentTool.name();
    }

    @Override
    public String getDescription() {
        return agentTool.description();
    }

    @Override
    public RiskLevel getRiskLevel() {
        return agentTool.riskLevel();
    }

    @Override
    public Class<?> getParameterClass() {
        return parameterClass;
    }

    @Override
    public ToolResult execute(Long userId, Object params) {
        @SuppressWarnings("unchecked")
        T typedParams = (T) params;
        return executeInternal(userId, typedParams);
    }

    protected abstract ToolResult executeInternal(Long userId, T params);
}
