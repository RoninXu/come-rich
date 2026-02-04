package com.finance.planner.ai.agent.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class DateParser {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy.M.d")
    );

    private DateParser() {
    }

    public static LocalDate parseUserDate(String input) {
        return parseUserDate(input, Clock.systemDefaultZone());
    }

    static LocalDate parseUserDate(String input, Clock clock) {
        if (input == null || input.isBlank()) {
            throw new DateTimeParseException("Date is blank", String.valueOf(input), 0);
        }

        String normalized = input.trim();
        return switch (normalized) {
            case "今天", "今日", "today" -> LocalDate.now(clock);
            case "昨天", "昨日", "yesterday" -> LocalDate.now(clock).minusDays(1);
            case "前天" -> LocalDate.now(clock).minusDays(2);
            case "明天", "tomorrow" -> LocalDate.now(clock).plusDays(1);
            case "后天" -> LocalDate.now(clock).plusDays(2);
            default -> parseFormattedDate(normalized);
        };
    }

    private static LocalDate parseFormattedDate(String input) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new DateTimeParseException("Unsupported date format", input, 0);
    }
}
