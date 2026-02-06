package com.finance.planner.ai.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agent_tool_metrics", indexes = {
        @Index(name = "idx_tool_metrics_user", columnList = "user_id"),
        @Index(name = "idx_tool_metrics_tool", columnList = "tool_name"),
        @Index(name = "idx_tool_metrics_time", columnList = "executed_at")
})
public class AgentToolMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "latency_ms", nullable = false)
    private Integer latencyMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Boolean cached = false;

    @CreationTimestamp
    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;
}
