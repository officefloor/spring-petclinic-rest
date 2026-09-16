package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * The single definition of an owner's <em>tenure</em>: the number of whole fiscal years elapsed
 * from their registration date up to today, where fiscal years start on 1 July (see
 * {@link FiscalYear}). An owner registered within the current fiscal year has zero tenure; the
 * count grows by one each time a 1 July boundary is crossed.
 *
 * <p>Used by the response mapper to decide the tenure-gated top membership level. Unknown or
 * future registration dates yield zero, so they can never satisfy a tenure threshold.
 */
public final class Tenure {

    private Tenure() {
    }

    /** Whole fiscal years between {@code registrationDate} and today, or 0 when the date is unknown
     * or lies in a later fiscal year than today. */
    public static long fiscalYears(LocalDate registrationDate) {
        if (registrationDate == null) {
            return 0;
        }
        return Math.max(FiscalYear.of(LocalDate.now()) - FiscalYear.of(registrationDate), 0);
    }
}
