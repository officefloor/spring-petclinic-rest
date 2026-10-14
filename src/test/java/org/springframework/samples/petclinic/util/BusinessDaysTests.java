package org.springframework.samples.petclinic.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class BusinessDaysTests {

    @Test
    void weekdaysAreUnchanged() {
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-10-12"))).isEqualTo("2026-10-12");
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-10-16"))).isEqualTo("2026-10-16");
    }

    @Test
    void weekendRollsForwardToMonday() {
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-10-17"))).isEqualTo("2026-10-19");
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-10-18"))).isEqualTo("2026-10-19");
    }

    @Test
    void weekendRollsAcrossYearBoundary() {
        assertThat(BusinessDays.rollForward(LocalDate.parse("2033-01-01"))).isEqualTo("2033-01-03");
        assertThat(BusinessDays.rollForward(LocalDate.parse("2034-12-31"))).isEqualTo("2035-01-01");
    }

    @Test
    void nullStaysNull() {
        assertThat(BusinessDays.rollForward(null)).isNull();
    }
}
