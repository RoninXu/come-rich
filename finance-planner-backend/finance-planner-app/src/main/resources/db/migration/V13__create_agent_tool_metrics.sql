CREATE TABLE agent_tool_metrics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    session_id VARCHAR(100) NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    success BOOLEAN NOT NULL,
    latency_ms INT NOT NULL,
    error_message TEXT,
    cached BOOLEAN DEFAULT false,
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tool_metrics_user ON agent_tool_metrics(user_id);
CREATE INDEX idx_tool_metrics_tool ON agent_tool_metrics(tool_name);
CREATE INDEX idx_tool_metrics_time ON agent_tool_metrics(executed_at);
