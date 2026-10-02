package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * The business rule for the organisation's fiscal year, which starts on 1 July.
 *
 * <p>This is the single authoritative home for the fiscal-year boundary: both the member id (whose
 * {@code FY} segment is built from a registration date in {@code ClinicServiceImpl}) and the
 * read-only {@code fiscalYear}/{@code membershipPoints} fields (derived in {@code OwnerMapper})
 * resolve the fiscal year through here, so the boundary cannot drift between the two.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * Returns the calendar year in which the fiscal year containing {@code date} begins. The fiscal
     * year starts on 1 July, so dates in July through December belong to the fiscal year starting in
     * their own calendar year, while dates in January through June belong to the fiscal year that
     * started in the previous calendar year.
     */
    public static int startYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
    }
}
