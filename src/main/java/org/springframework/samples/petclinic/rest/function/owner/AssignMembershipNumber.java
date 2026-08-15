package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.FiscalYears;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code membershipNumber}, formatted
 * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of the fiscal year
 * (starting 1 July) derived from the business-day-adjusted {@code registrationDate}, zero-padded
 * (e.g. {@code 'NSW-1A2B3C4D-M26'}). The {@code YY} therefore matches the owner's {@code fiscalYear}.
 *
 * <p>Runs after {@link AssignCustomerCode} has set the {@code customerCode} and after
 * {@link BuildOwner} has set the {@code registrationDate}, and before {@link SaveOwner}. Derived
 * entirely from the owner's own fields, so it is seed-independent. {@code @Val} yields the built
 * owner so the number is mutated in place and persisted by the save step.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = Math.floorMod(FiscalYears.yearOf(owner.getRegistrationDate()), 100);
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
