package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Unit tests for the fiscal-year basis (fiscal year starting 1 July). */
class FiscalYearTest {

    @Test
    void julyStartsTheNextFiscalYear() {
        // 1 July 2026 opens the fiscal year ending in 2027.
        assertEquals(2027, FiscalYear.of(LocalDate.of(2026, 7, 1)));
    }

    @Test
    void juneEndsTheCurrentFiscalYear() {
        // 30 June 2026 is the last day of the fiscal year ending in 2026.
        assertEquals(2026, FiscalYear.of(LocalDate.of(2026, 6, 30)));
    }

    @Test
    void januaryBelongsToTheFiscalYearEndingThatYear() {
        assertEquals(2026, FiscalYear.of(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void labelIsFyPlusTwoDigitEndingYear() {
        assertEquals("FY27", FiscalYear.labelOf(LocalDate.of(2026, 9, 21)));
        assertEquals("FY26", FiscalYear.labelOf(LocalDate.of(2026, 6, 30)));
    }

    @Test
    void shortYearIsTheEndingYearModuloHundred() {
        assertEquals(27, FiscalYear.shortYearOf(LocalDate.of(2026, 9, 21)));
        assertEquals(0, FiscalYear.shortYearOf(LocalDate.of(1999, 8, 1)));
    }

    @Test
    void sameFiscalYearHasZeroElapsed() {
        // Both dates fall in the fiscal year ending 2027 (Jul 2026 - Jun 2027).
        assertEquals(0, FiscalYear.elapsed(LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30)));
    }

    @Test
    void crossingOneFiscalBoundaryIsOneElapsedYear() {
        // 30 June 2026 (FY ending 2026) to 1 July 2026 (FY ending 2027): one boundary.
        assertEquals(1, FiscalYear.elapsed(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 1)));
    }

    @Test
    void elapsedCountsWholeFiscalYears() {
        assertEquals(3, FiscalYear.elapsed(LocalDate.of(2024, 2, 1), LocalDate.of(2026, 9, 1)));
    }
}
