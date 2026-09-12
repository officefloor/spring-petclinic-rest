package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Derives an owner's fiscal year from their (business-day-adjusted) registrationDate.
 *
 * <p>The fiscal year starts on 1 July and is labelled by the calendar year in which it
 * ends: a date on or after 1 July falls in the fiscal year ending the following 30 June.
 * For example 1 July 2025 and 12 September 2025 both fall in fiscal year 2026, while 30
 * June 2025 falls in fiscal year 2025.
 *
 * <p>{@link #of(Owner)} formats this as {@code 'FY<YY>'} (the two-digit ending year, e.g.
 * {@code 'FY26'}), reading the two-digit FY segment back out of the owner's {@code memberId}
 * when present (that segment was itself built from {@link #shortYear(LocalDate)} at
 * creation) and falling back to the registrationDate for records without one.
 * {@link #shortYear(LocalDate)} exposes the two-digit ending year on its own and
 * {@link #yearOf(LocalDate)} the four-digit ending year, for callers that build their own
 * segments (such as the memberId).
 */
public final class FiscalYear {

    /** The month on whose first day the fiscal year begins. */
    private static final int START_MONTH = Month.JULY.getValue();

    private FiscalYear() {
    }

    /** The fiscal year label ({@code 'FY<YY>'}) for the owner, or null when unregistered. */
    public static String of(Owner owner) {
        String fromMemberId = shortYearFromMemberId(owner.getMemberId());
        if (fromMemberId != null) {
            return "FY" + fromMemberId;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return "FY" + shortYear(registrationDate);
    }

    /**
     * The two-digit FY segment of a {@code memberId} ({@code <REGION><FY><HASH8><CHK>}):
     * the two digits immediately after the leading letter REGION. Null when the memberId is
     * null or does not hold two digits at that position.
     */
    private static String shortYearFromMemberId(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        if (i + 2 > memberId.length()) {
            return null;
        }
        String fy = memberId.substring(i, i + 2);
        if (Character.isDigit(fy.charAt(0)) && Character.isDigit(fy.charAt(1))) {
            return fy;
        }
        return null;
    }

    /** The two-digit ending year ({@code 'YY'}) of the fiscal year that {@code date} falls in. */
    public static String shortYear(LocalDate date) {
        return String.format("%02d", yearOf(date) % 100);
    }

    /** The fiscal year (the calendar year it ends in) that {@code date} falls in. */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= START_MONTH ? date.getYear() + 1 : date.getYear();
    }
}
