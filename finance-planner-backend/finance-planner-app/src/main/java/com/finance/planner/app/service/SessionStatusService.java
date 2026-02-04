package com.finance.planner.app.service;

import com.finance.planner.ai.config.LlmProviderProperties;
import com.finance.planner.ai.service.LlmProviderManager;
import com.finance.planner.app.dto.SessionStatusDto;
import com.finance.planner.ai.time.TimeContext;
import com.finance.planner.ai.time.TimeContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionStatusService {

    private final TimeContextProvider timeContextProvider;
    private final LlmProviderManager llmProviderManager;

    public SessionStatusDto buildStatus(Long userId, String sessionId) {
        TimeContext timeContext = timeContextProvider.getTimeContext(userId, sessionId);
        LlmProviderProperties config = llmProviderManager.getActiveConfig();

        return SessionStatusDto.builder()
                .sessionId(sessionId)
                .userId(userId)
                .serverTime(timeContext.getServerTime())
                .serverDate(timeContext.getServerDate())
                .timezone(timeContext.getTimezone())
                .clockSource(timeContext.getClockSource())
                .activeProvider(llmProviderManager.getActiveProvider())
                .model(config != null ? config.getModel() : null)
                .build();
    }
}
