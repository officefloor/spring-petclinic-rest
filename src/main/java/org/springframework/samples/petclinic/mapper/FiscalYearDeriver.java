package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July and is
 * identified by the calendar year in which it ends, following the Australian
 * convention: a date in July through December falls in the <em>next</em> calendar
 * year's fiscal year, while January through June stays in the current one.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic mapping method and apply it to
 * unrelated fields; the mapper references it only through explicit expressions.
 */
public final class FiscalYearDeriver {

    private FiscalYearDeriver() {
    }

    /**
     * Returns the fiscal year (the calendar year in which it ends) for the given
     * date. July through December map to {@code year + 1}; January through June to
     * {@code year}.
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Returns the fiscal-year label {@code 'FY<YY>'}, where {@code YY} is the last
     * two digits of {@link #fiscalYear(LocalDate)}.
     */
    public static String fiscalYearLabel(LocalDate date) {
        return String.format("FY%02d", fiscalYear(date) % 100);
    }

    /**
     * Returns the number of whole fiscal years elapsed from {@code from} to
     * {@code to}, i.e. the difference of their fiscal years.
     */
    public static int elapsedFiscalYears(LocalDate from, LocalDate to) {
        return fiscalYear(to) - fiscalYear(from);
    }
}
