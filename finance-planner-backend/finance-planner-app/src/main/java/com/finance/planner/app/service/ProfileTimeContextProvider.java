package com.finance.planner.app.service;

import com.finance.planner.ai.time.TimeContext;
import com.finance.planner.ai.time.TimeContextProvider;
import com.finance.planner.career.dto.UserProfileDto;
import com.finance.planner.career.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Primary
@RequiredArgsConstructor
public class ProfileTimeContextProvider implements TimeContextProvider {

    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    private final UserProfileService userProfileService;

    @Override
    public TimeContext getTimeContext(Long userId, String sessionId) {
        String timezone = resolveTimezone(userId);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return TimeContext.builder()
                .serverTime(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .serverDate(now.toLocalDate().toString())
                .timezone(timezone)
                .clockSource("application")
                .build();
    }

    private String resolveTimezone(Long userId) {
        UserProfileDto profile = userProfileService.getProfile(userId);
        if (profile == null || profile.getTimezone() == null || profile.getTimezone().isBlank()) {
            return DEFAULT_TIMEZONE;
        }
        try {
            ZoneId.of(profile.getTimezone());
            return profile.getTimezone();
        } catch (Exception ex) {
            return DEFAULT_TIMEZONE;
        }
    }
}
