package com.finance.planner.ai.agent.service;

import com.finance.planner.ai.agent.dto.metrics.AgentErrorMetricDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsOverviewDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsTimelinePointDto;
import com.finance.planner.ai.agent.dto.metrics.AgentToolMetricDto;
import com.finance.planner.ai.agent.repository.AgentToolMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentMetricsQueryService {

    private final AgentToolMetricsRepository metricsRepository;

    public AgentMetricsOverviewDto getOverview(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Object[] row = metricsRepository.getOverviewStats(userId, startTime, endTime);
        long totalCalls = toLong(row[0]);
        long successfulCalls = toLong(row[1]);
        double averageLatencyMs = round2(toDouble(row[2]));
        long cachedCalls = toLong(row[3]);
        long totalSessions = toLong(row[4]);

        return AgentMetricsOverviewDto.builder()
                .totalCalls(totalCalls)
                .successfulCalls(successfulCalls)
                .failedCalls(totalCalls - successfulCalls)
                .totalSessions(totalSessions)
                .successRate(rate(successfulCalls, totalCalls))
                .averageLatencyMs(averageLatencyMs)
                .cacheHitRate(rate(cachedCalls, totalCalls))
                .build();
    }

    public List<AgentToolMetricDto> getToolMetrics(Long userId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<Object[]> rows = metricsRepository.getToolStats(
                userId,
                startTime,
                endTime,
                PageRequest.of(0, Math.max(1, limit))
        );

        List<AgentToolMetricDto> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            long totalCalls = toLong(row[1]);
            long successfulCalls = toLong(row[2]);
            long cachedCalls = toLong(row[4]);
            result.add(AgentToolMetricDto.builder()
                    .toolName((String) row[0])
                    .totalCalls(totalCalls)
                    .successfulCalls(successfulCalls)
                    .failedCalls(totalCalls - successfulCalls)
                    .successRate(rate(successfulCalls, totalCalls))
                    .averageLatencyMs(round2(toDouble(row[3])))
                    .cacheHitRate(rate(cachedCalls, totalCalls))
                    .build());
        }

        return result;
    }

    public List<AgentMetricsTimelinePointDto> getTimeline(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> rows = metricsRepository.getTimelineStats(userId, startTime, endTime);
        List<AgentMetricsTimelinePointDto> result = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            LocalDate date = resolveDate(row[0]);
            long totalCalls = toLong(row[1]);
            long successfulCalls = toLong(row[2]);

            result.add(AgentMetricsTimelinePointDto.builder()
                    .date(date)
                    .totalCalls(totalCalls)
                    .successfulCalls(successfulCalls)
                    .failedCalls(totalCalls - successfulCalls)
                    .successRate(rate(successfulCalls, totalCalls))
                    .averageLatencyMs(round2(toDouble(row[3])))
                    .build());
        }

        return result;
    }

    public List<AgentErrorMetricDto> getErrorStats(Long userId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<Object[]> rows = metricsRepository.getErrorStats(
                userId,
                startTime,
                endTime,
                PageRequest.of(0, Math.max(1, limit))
        );

        List<AgentErrorMetricDto> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            String message = row[0] == null || ((String) row[0]).isBlank() ? "未知错误" : (String) row[0];
            result.add(AgentErrorMetricDto.builder()
                    .errorMessage(message)
                    .count(toLong(row[1]))
                    .build());
        }
        return result;
    }

    private static long toLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }

    private static double toDouble(Object value) {
        if (value == null) return 0D;
        return ((Number) value).doubleValue();
    }

    private static double rate(long numerator, long denominator) {
        if (denominator <= 0) return 0D;
        return round2((double) numerator * 100D / (double) denominator);
    }

    private static double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private static LocalDate resolveDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}

