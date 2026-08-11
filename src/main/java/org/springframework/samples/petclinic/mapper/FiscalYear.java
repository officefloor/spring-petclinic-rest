package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Puts date-derived values on a fiscal-year basis. The fiscal year starts on 1 July, so a date in
 * July or later belongs to the fiscal year ending the following calendar year; a date in January to
 * June belongs to the fiscal year ending in its own calendar year. Fiscal years are labelled
 * {@code 'FY<YY>'} after that ending year (e.g. 15 June 2026 and 1 July 2025 are both in 'FY26').
 *
 * <p>{@link #labelOf(Owner)} yields the label for an owner's business-day-adjusted
 * {@code registrationDate}; {@link #elapsedSince(LocalDate, LocalDate)} counts whole fiscal years
 * between two dates. Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper for an
 * implicit mapping method.
 */
public final class FiscalYear {

    /** The month a fiscal year starts on. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /** The calendar year the fiscal year containing {@code date} ends in. */
    public static int endYearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The {@code 'FY<YY>'} label for {@code date}, YY being the last two digits of its fiscal year. */
    public static String labelOf(LocalDate date) {
        return String.format("FY%02d", endYearOf(date) % 100);
    }

    /** The {@code 'FY<YY>'} label for an owner's registrationDate, or {@code null} when absent. */
    public static String labelOf(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate != null ? labelOf(registrationDate) : null;
    }

    /** The number of whole fiscal years elapsed from {@code from} to {@code to}. */
    public static int elapsedSince(LocalDate from, LocalDate to) {
        return endYearOf(to) - endYearOf(from);
    }
}
