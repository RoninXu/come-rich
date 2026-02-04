package com.finance.planner.ai.agent.util;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RelativeDateResolver {

    private static final Pattern RELATIVE_PATTERN = Pattern.compile("(今天|今日|昨天|昨日|明天|前天|后天|today|yesterday|tomorrow)");
    private static final Pattern ABSOLUTE_PATTERN = Pattern.compile("\\b\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}\\b");

    private RelativeDateResolver() {
    }

    public static Optional<LocalDate> resolveFromMessage(String message) {
        return resolveFromMessage(message, Clock.systemDefaultZone());
    }

    public static Optional<LocalDate> resolveFromMessage(String message, Clock clock) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }
        if (ABSOLUTE_PATTERN.matcher(message).find()) {
            return Optional.empty();
        }
        Matcher matcher = RELATIVE_PATTERN.matcher(message);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(DateParser.parseUserDate(matcher.group(1), clock));
    }
}
