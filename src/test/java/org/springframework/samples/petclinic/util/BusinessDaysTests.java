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
    void weekdayHolidayRollsToNextBusinessDay() {
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-01-01"))).isEqualTo("2026-01-02");
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-01-26"))).isEqualTo("2026-01-27");
    }

    @Test
    void weekendHolidayRollsToMonday() {
        // 2026-04-25 is a Saturday
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-04-25"))).isEqualTo("2026-04-27");
    }

    @Test
    void rollChainsAcrossHolidaysAndWeekends() {
        // Fri 25 Dec (holiday) -> weekend -> Mon 28 Dec (holiday) -> Tue 29 Dec
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-12-25"))).isEqualTo("2026-12-29");
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-12-26"))).isEqualTo("2026-12-29");
        assertThat(BusinessDays.rollForward(LocalDate.parse("2026-12-28"))).isEqualTo("2026-12-29");
    }

    @Test
    void sameDateInOtherYearIsNotAHoliday() {
        assertThat(BusinessDays.rollForward(LocalDate.parse("2027-01-26"))).isEqualTo("2027-01-26");
    }

    @Test
    void nullStaysNull() {
        assertThat(BusinessDays.rollForward(null)).isNull();
    }
}
