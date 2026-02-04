package com.finance.planner.ai.agent.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateParserTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-02-04T10:00:00Z"),
            ZoneId.of("UTC")
    );

    @Test
    void parsesRelativeDates() {
        assertThat(DateParser.parseUserDate("今天", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 4));
        assertThat(DateParser.parseUserDate("昨天", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 3));
        assertThat(DateParser.parseUserDate("明天", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 5));
        assertThat(DateParser.parseUserDate("前天", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(DateParser.parseUserDate("后天", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 6));
        assertThat(DateParser.parseUserDate("today", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 4));
    }

    @Test
    void parsesFormattedDates() {
        assertThat(DateParser.parseUserDate("2026-02-04", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 4));
        assertThat(DateParser.parseUserDate("2026/2/4", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 4));
        assertThat(DateParser.parseUserDate("2026.2.4", FIXED_CLOCK)).isEqualTo(LocalDate.of(2026, 2, 4));
    }

    @Test
    void rejectsUnsupportedFormats() {
        assertThatThrownBy(() -> DateParser.parseUserDate("02-04-2026", FIXED_CLOCK))
                .isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> DateParser.parseUserDate(" ", FIXED_CLOCK))
                .isInstanceOf(DateTimeParseException.class);
    }
}
