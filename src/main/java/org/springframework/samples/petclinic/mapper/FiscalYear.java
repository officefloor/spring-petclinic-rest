package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives fiscal-year values from a date. The fiscal year runs 1 July to 30 June and is identified
 * by the calendar year in which it ends, so 1 July 2025 to 30 June 2026 is fiscal year 2026 and its
 * label is {@code FY26}.
 *
 * <p>Fed the owner's persisted (business-day-adjusted) registration date, this is the single source
 * of truth for the member id's FY segment and the tenure count in {@link MembershipLevel}. The
 * {@code fiscalYear} response field is in turn read back off that member id (see
 * {@link #labelOfMemberId(String)}), so they always agree.
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
     * The fiscal-year label {@code FY<YY>} carried by a {@code '<REGION><FY><HASH8><CHK>'} member id
     * &mdash; its two-digit year segment, which directly follows the leading region letters. So the
     * response's {@code fiscalYear} references the unified member id rather than recomputing from the
     * registration date, and the two cannot disagree. Returns {@code null} when the member id is
     * absent or carries no two-digit year segment.
     */
    public static String labelOfMemberId(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        if (i + 2 > memberId.length()
                || !Character.isDigit(memberId.charAt(i)) || !Character.isDigit(memberId.charAt(i + 1))) {
            return null;
        }
        return "FY" + memberId.substring(i, i + 2);
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}, i.e. the difference
     * of their fiscal-year ordinals. Zero when both fall in the same fiscal year.
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return ordinal(to) - ordinal(from);
    }
}
