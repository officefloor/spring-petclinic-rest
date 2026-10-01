package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>},
 * where {@code customerCode} is the owner's previously assigned region-and-hash customer code and
 * {@code YY} is the last two digits of the registration date year (e.g.
 * {@code NSW-1A2B3C4D-M26}).
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(String.format("%s-M%s", owner.getCustomerCode(), yy));
    }
}
