package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date. The fiscal year runs 1 July to 30 June and is identified
 * by the calendar year in which it ends, so 1 July 2025 to 30 June 2026 is fiscal year 2026 and its
 * label is {@code FY26}.
 *
 * <p>Fed the owner's persisted (business-day-adjusted) registration date, this is the single source
 * of truth for the {@code fiscalYear} response field, the membership number's year segment and the
 * tenure count in {@link MembershipLevel}, so they always agree.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not mistake
 * it for an implicit mapping method.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * The fiscal-year ordinal for {@code date}: the calendar year in which its fiscal year (1 July
     * to 30 June) ends. Dates in July or later belong to the fiscal year ending the next calendar
     * year; earlier dates belong to the one ending in the same calendar year.
     */
    public static int ordinal(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The two-digit year segment of {@code date}'s fiscal year (its ordinal modulo 100). */
    public static int yearSegment(LocalDate date) {
        return ordinal(date) % 100;
    }

    /** The fiscal-year label {@code FY<YY>} for {@code date}, e.g. {@code FY26}. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yearSegment(date));
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}, i.e. the difference
     * of their fiscal-year ordinals. Zero when both fall in the same fiscal year.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return ordinal(to) - ordinal(from);
    }
}
