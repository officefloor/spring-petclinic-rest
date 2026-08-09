package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Derives fiscal-year values from a date. The fiscal year starts on 1 July and is identified
 * by the calendar year in which it ends, so 1 July 2026 to 30 June 2027 is fiscal year 2027.
 * A date on or after 1 July therefore belongs to the fiscal year {@code year + 1}; a date
 * before 1 July belongs to the fiscal year {@code year}.
 *
 * <p>Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit mapping
 * method and apply it to every property.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * Return the fiscal year (a full calendar year, e.g. {@code 2027}) that {@code date} falls
     * in. The fiscal year starts on 1 July and is named by the calendar year it ends in.
     *
     * @param date the date to classify
     * @return the fiscal year value
     */
    public static int yearValue(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Return the fiscal-year label {@code 'FY<YY>'} for {@code date}, where {@code YY} is the
     * last two digits of the fiscal year (e.g. {@code 'FY27'}), or {@code null} when
     * {@code date} is absent so the field is omitted from the response.
     *
     * @param date the date to label (may be {@code null})
     * @return the {@code 'FY<YY>'} label, or {@code null} when {@code date} is null
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", Math.floorMod(yearValue(date), 100));
    }

    /**
     * Return the number of fiscal years elapsed between {@code from} and {@code to}, i.e. the
     * difference of their fiscal-year values. Zero when both dates fall in the same fiscal
     * year, and negative when {@code to} precedes {@code from}'s fiscal year.
     *
     * @param from the earlier date (e.g. the registration date)
     * @param to   the later date (e.g. today)
     * @return the count of elapsed fiscal years
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return yearValue(to) - yearValue(from);
    }
}
