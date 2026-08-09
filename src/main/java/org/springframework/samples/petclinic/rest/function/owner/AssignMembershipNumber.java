package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'} where
 * YY is the last two digits of the FISCAL YEAR of the {@code registrationDate} (the fiscal year
 * starts on 1 July, so a registration date on or after 1 July belongs to the next calendar year,
 * e.g. 'NSW-1A2B3C4D-M27' for a 2026-07-01 registration).
 *
 * <p>Runs after {@link AssignCustomerCode} (so the customer code exists) and {@link BuildOwner}
 * (so the registration date is set), and before {@link SaveOwner}, mutating the not-yet-persisted
 * owner in place.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = fiscalYearOf(owner.getRegistrationDate()) % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }

    /**
     * The fiscal year a date falls in, as a full calendar year. The fiscal year starts on 1 July, so
     * a date in July or later belongs to the next calendar year and an earlier date to the current
     * calendar year.
     */
    private static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }
}
