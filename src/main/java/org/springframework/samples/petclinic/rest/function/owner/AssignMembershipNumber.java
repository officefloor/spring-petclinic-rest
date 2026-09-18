package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a newly built owner its {@code membershipNumber}, formatted
 * {@code <customerCode>-M<YY>}, where YY is the last two digits of the registration
 * date year (e.g. {@code NSW-1A2B3C4D-M26}). Runs after both the customer code and the
 * registration date have been assigned, so both inputs are present.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
