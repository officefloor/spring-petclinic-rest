package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where
 * customerCode is the owner's already-assigned region-and-hash customer code and YY is the last two
 * digits of the registrationDate year, zero-padded (e.g. {@code NSW-1A2B3C4D-M26}).
 *
 * <p>Runs after {@link AssignCustomerCode} (which sets the customer code) and after
 * {@link BuildOwner} (which defaults the registration date), so both inputs are present.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
