package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July and is named by the calendar year in which
 * it ends, following the Australian convention (e.g. the fiscal year running 1 July 2025 to 30 June
 * 2026 is {@code FY26}). Dates fed to these helpers are the business-day-adjusted registration date.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * The calendar year in which the fiscal year containing {@code date} ends: the date's own year
     * for January to June, the following year for July to December.
     */
    public static int endingYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal-year label {@code FY<YY>} for {@code date} (the last two digits of the fiscal
     * year's ending year), or {@code null} when {@code date} is absent.
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", endingYear(date) % 100);
    }

    /**
     * The number of fiscal years elapsed between {@code from} and {@code to}: the difference of
     * their fiscal-year ending years, i.e. the count of 1 July boundaries crossed.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return endingYear(to) - endingYear(from);
    }
}
