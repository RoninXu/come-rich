package com.finance.planner.ai.agent.repository;

import com.finance.planner.ai.agent.entity.AgentToolMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgentToolMetricsRepository extends JpaRepository<AgentToolMetrics, Long> {

    List<AgentToolMetrics> findByUserIdOrderByExecutedAtDesc(Long userId);

    List<AgentToolMetrics> findBySessionId(String sessionId);

    List<AgentToolMetrics> findByToolNameAndExecutedAtAfter(String toolName, LocalDateTime after);

    @Query("""
            SELECT COUNT(m),
                   SUM(CASE WHEN m.success = true THEN 1 ELSE 0 END),
                   AVG(m.latencyMs),
                   SUM(CASE WHEN m.cached = true THEN 1 ELSE 0 END),
                   COUNT(DISTINCT m.sessionId)
            FROM AgentToolMetrics m
            WHERE m.userId = :userId
              AND m.executedAt BETWEEN :startTime AND :endTime
            """)
    Object[] getOverviewStats(@Param("userId") Long userId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT m.toolName,
                   COUNT(m),
                   SUM(CASE WHEN m.success = true THEN 1 ELSE 0 END),
                   AVG(m.latencyMs),
                   SUM(CASE WHEN m.cached = true THEN 1 ELSE 0 END)
            FROM AgentToolMetrics m
            WHERE m.userId = :userId
              AND m.executedAt BETWEEN :startTime AND :endTime
            GROUP BY m.toolName
            ORDER BY COUNT(m) DESC
            """)
    List<Object[]> getToolStats(@Param("userId") Long userId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                Pageable pageable);

    @Query("""
            SELECT FUNCTION('DATE', m.executedAt),
                   COUNT(m),
                   SUM(CASE WHEN m.success = true THEN 1 ELSE 0 END),
                   AVG(m.latencyMs)
            FROM AgentToolMetrics m
            WHERE m.userId = :userId
              AND m.executedAt BETWEEN :startTime AND :endTime
            GROUP BY FUNCTION('DATE', m.executedAt)
            ORDER BY FUNCTION('DATE', m.executedAt)
            """)
    List<Object[]> getTimelineStats(@Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT m.errorMessage, COUNT(m)
            FROM AgentToolMetrics m
            WHERE m.userId = :userId
              AND m.success = false
              AND m.executedAt BETWEEN :startTime AND :endTime
            GROUP BY m.errorMessage
            ORDER BY COUNT(m) DESC
            """)
    List<Object[]> getErrorStats(@Param("userId") Long userId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 Pageable pageable);
}
