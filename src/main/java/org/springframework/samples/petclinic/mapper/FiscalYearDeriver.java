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
     * Returns the fiscal-year label {@code 'FY<YY>'} read from an owner's
     * {@code '<REGION><FY><HASH8><CHK>'} memberId, where {@code YY} is the two-digit
     * {@code <FY>} segment that immediately follows the version-2 region prefix (e.g.
     * {@code 'V2NSW'}). Returns {@code null} when the memberId is {@code null} or too
     * short to carry an FY segment.
     */
    public static String fiscalYearLabel(String memberId) {
        if (memberId == null) {
            return null;
        }
        int fyStart = LocalityDeriver.embeddedRegion(memberId).length();
        if (memberId.length() < fyStart + 2) {
            return null;
        }
        return "FY" + memberId.substring(fyStart, fyStart + 2);
    }

    /**
     * Returns the number of whole fiscal years elapsed from {@code from} to
     * {@code to}, i.e. the difference of their fiscal years.
     */
    public static int elapsedFiscalYears(LocalDate from, LocalDate to) {
        return fiscalYear(to) - fiscalYear(from);
    }
}
