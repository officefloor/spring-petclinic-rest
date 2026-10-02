package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code membershipNumber} formatted {@code <customerCode>-M<YY>}, where
 * {@code <customerCode>} is the owner's region-and-hash customer code and YY is the last two digits of
 * the registrationDate year (e.g. {@code NSW-A1B2C3D4-M26}). Runs after {@link AssignCustomerCode} so
 * the customer code is already assigned, and after {@link BuildOwner} which defaults the registration
 * date.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(String.format("%s-M%s", owner.getCustomerCode(), yy));
    }
}
