package com.finance.planner.ai.time;

import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DefaultTimeContextProvider implements TimeContextProvider {

    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    @Override
    public TimeContext getTimeContext(Long userId, String sessionId) {
        ZoneId zoneId = ZoneId.of(DEFAULT_TIMEZONE);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return TimeContext.builder()
                .serverTime(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .serverDate(now.toLocalDate().toString())
                .timezone(DEFAULT_TIMEZONE)
                .clockSource("application")
                .build();
    }
}
