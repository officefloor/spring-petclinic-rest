package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where
 * {@code <customerCode>} is the owner's already-assigned customer code and {@code YY} is the
 * last two digits of the {@code registrationDate} year (e.g. {@code SMI-0007-M26}).
 *
 * <p>Runs after {@link AssignCustomerCode} (so the customer code exists) and after
 * {@link BuildOwner} (so the registration date is set), and before {@link SaveOwner}.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
