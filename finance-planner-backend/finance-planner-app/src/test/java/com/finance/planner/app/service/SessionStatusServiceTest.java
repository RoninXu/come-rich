package com.finance.planner.app.service;

import com.finance.planner.ai.config.LlmProviderProperties;
import com.finance.planner.ai.service.LlmProviderManager;
import com.finance.planner.ai.time.TimeContext;
import com.finance.planner.ai.time.TimeContextProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SessionStatusServiceTest {

    @Test
    void usesDefaultTimezoneWhenProfileMissing() {
        TimeContextProvider timeContextProvider = Mockito.mock(TimeContextProvider.class);
        LlmProviderManager providerManager = Mockito.mock(LlmProviderManager.class);
        when(timeContextProvider.getTimeContext(1L, "session-1"))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .serverTime("2026-02-04T13:00:00+08:00")
                        .timezone("Asia/Shanghai")
                        .clockSource("application")
                        .build());

        LlmProviderProperties config = new LlmProviderProperties();
        config.setModel("test-model");
        when(providerManager.getActiveConfig()).thenReturn(config);
        when(providerManager.getActiveProvider()).thenReturn("deepseek");

        SessionStatusService service = new SessionStatusService(timeContextProvider, providerManager);

        var status = service.buildStatus(1L, "session-1");

        assertThat(status.getTimezone()).isEqualTo("Asia/Shanghai");
        assertThat(status.getActiveProvider()).isEqualTo("deepseek");
        assertThat(status.getModel()).isEqualTo("test-model");
        assertThat(status.getServerDate()).isNotBlank();
        assertThat(status.getServerTime()).isNotBlank();
    }

    @Test
    void fallsBackToDefaultForInvalidTimezone() {
        TimeContextProvider timeContextProvider = Mockito.mock(TimeContextProvider.class);
        LlmProviderManager providerManager = Mockito.mock(LlmProviderManager.class);
        when(timeContextProvider.getTimeContext(2L, null))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .serverTime("2026-02-04T13:00:00+08:00")
                        .timezone("Asia/Shanghai")
                        .clockSource("application")
                        .build());

        LlmProviderProperties config = new LlmProviderProperties();
        config.setModel("test-model");
        when(providerManager.getActiveConfig()).thenReturn(config);
        when(providerManager.getActiveProvider()).thenReturn("moonshot");

        SessionStatusService service = new SessionStatusService(timeContextProvider, providerManager);

        var status = service.buildStatus(2L, null);

        assertThat(status.getTimezone()).isEqualTo("Asia/Shanghai");
        assertThat(status.getActiveProvider()).isEqualTo("moonshot");
    }
}
