package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Single source of truth for an owner's fiscal-year derivations. The fiscal year starts on 1 July: a
 * date on or after 1 July belongs to the fiscal year that started that calendar year, an earlier date
 * to the one that started the previous calendar year. A fiscal year is identified by the calendar year
 * it starts in, so the fiscal year running 1 July 2026 - 30 June 2027 is year 2026 and labelled
 * {@code FY26}.
 *
 * <p>This is a model-level derivation of an {@link Owner} (like {@link MemberId} and
 * {@link MembershipLevel}): it is consumed by the response mapper (the {@code fiscalYear} field) and by
 * the other date-derived values that moved to a fiscal-year basis - the member id's {@code FY} segment
 * ({@link MemberId}) and tenure ({@link MembershipLevel}) - so it lives in the model layer rather than
 * beside the pipeline functions.
 *
 * <p>Every value is computed from the owner's stored registration date, which the create pipeline has
 * already rolled forward to a business day, so these derivations are on a business-day-adjusted basis.
 */
public final class FiscalYear {

    /** The month a fiscal year starts on (1 July). */
    public static final Month START_MONTH = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year a date falls in, identified by the calendar year the fiscal year starts in. A
     * date on or after 1 July yields its own calendar year; an earlier date yields the previous one.
     */
    public static int startYearOf(LocalDate date) {
        return date.getMonthValue() >= START_MONTH.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /** The {@code fiscalYear} label {@code FY<YY>} of an owner, from its registration date. */
    public static String of(Owner owner) {
        return label(owner.getRegistrationDate());
    }

    /**
     * The two-digit fiscal-year start year ({@code YY}) for a date, zero-padded. The single rendering
     * of the fiscal year's two digits, shared by this class's {@link #label(LocalDate)} and by the
     * {@link MemberId} {@code FY} segment, so the two cannot drift apart.
     */
    public static String twoDigitStartYear(LocalDate date) {
        return String.format("%02d", startYearOf(date) % 100);
    }

    /** The {@code FY<YY>} label for a date, where {@code YY} is the two-digit fiscal-year start year. */
    private static String label(LocalDate date) {
        return "FY" + twoDigitStartYear(date);
    }
}
