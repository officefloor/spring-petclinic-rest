package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * The single definition of an owner's "tenure": the number of whole {@link FiscalYear fiscal
 * years} that have elapsed since the owner's registration date. An owner registered in the
 * current fiscal year has zero tenure; each 1 July boundary since registration adds one.
 * Provides the measure {@link AssignMembershipLevel} uses to gate the top membership level,
 * which is reserved for long-standing owners.
 */
final class Tenure {

    private Tenure() {
    }

    /** Whole fiscal years elapsed between {@code registrationDate} and {@code asOf}; zero when
     *  the date is unset or still in the future. */
    static long fiscalYears(LocalDate registrationDate, LocalDate asOf) {
        if (registrationDate == null) {
            return 0;
        }
        return FiscalYear.between(registrationDate, asOf);
    }
}
