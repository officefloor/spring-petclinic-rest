package org.springframework.samples.petclinic.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class FiscalYearsTests {

    @Test
    void fiscalYearStartsOnFirstOfJuly() {
        assertThat(FiscalYears.of(LocalDate.parse("2026-06-30"))).isEqualTo(2026);
        assertThat(FiscalYears.of(LocalDate.parse("2026-07-01"))).isEqualTo(2027);
        assertThat(FiscalYears.of(LocalDate.parse("2026-12-31"))).isEqualTo(2027);
        assertThat(FiscalYears.of(LocalDate.parse("2027-01-01"))).isEqualTo(2027);
    }

    @Test
    void labelIsTwoDigitFiscalYear() {
        assertThat(FiscalYears.label(LocalDate.parse("2026-10-14"))).isEqualTo("FY27");
        assertThat(FiscalYears.label(LocalDate.parse("2026-06-30"))).isEqualTo("FY26");
        assertThat(FiscalYears.label(LocalDate.parse("2099-07-01"))).isEqualTo("FY00");
        assertThat(FiscalYears.label(LocalDate.parse("2008-01-15"))).isEqualTo("FY08");
        assertThat(FiscalYears.label(null)).isNull();
    }

    @Test
    void betweenCountsElapsedFiscalYears() {
        assertThat(FiscalYears.between(LocalDate.parse("2026-06-30"), LocalDate.parse("2026-07-01"))).isEqualTo(1);
        assertThat(FiscalYears.between(LocalDate.parse("2026-07-01"), LocalDate.parse("2027-06-30"))).isZero();
        assertThat(FiscalYears.between(LocalDate.parse("2024-08-01"), LocalDate.parse("2026-10-14"))).isEqualTo(2);
    }
}
