package com.finance.planner.ai.agent.repository;

import com.finance.planner.ai.agent.entity.AgentToolMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgentToolMetricsRepository extends JpaRepository<AgentToolMetrics, Long> {

    List<AgentToolMetrics> findByUserIdOrderByExecutedAtDesc(Long userId);

    List<AgentToolMetrics> findBySessionId(String sessionId);

    List<AgentToolMetrics> findByToolNameAndExecutedAtAfter(String toolName, LocalDateTime after);
}
