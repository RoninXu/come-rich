package com.finance.planner.ai.agent.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeDateResolverTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-02-04T10:00:00Z"),
            ZoneId.of("UTC")
    );

    @Test
    void resolvesRelativeDateWhenNoAbsoluteDatePresent() {
        assertThat(RelativeDateResolver.resolveFromMessage("今天早餐22", FIXED_CLOCK))
                .contains(LocalDate.of(2026, 2, 4));
        assertThat(RelativeDateResolver.resolveFromMessage("昨天晚餐", FIXED_CLOCK))
                .contains(LocalDate.of(2026, 2, 3));
    }

    @Test
    void ignoresRelativeDateWhenAbsoluteDateProvided() {
        assertThat(RelativeDateResolver.resolveFromMessage("2026-02-01 早餐", FIXED_CLOCK))
                .isEmpty();
        assertThat(RelativeDateResolver.resolveFromMessage("2026/2/1 记账 今天", FIXED_CLOCK))
                .isEmpty();
    }
}
