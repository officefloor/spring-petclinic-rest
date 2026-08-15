package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code membershipNumber}, formatted
 * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of the
 * {@code registrationDate} year, zero-padded (e.g. {@code 'SYD-SMI-0007-M26'}).
 *
 * <p>Runs after {@link AssignCustomerCode} has set the {@code customerCode} and after
 * {@link BuildOwner} has set the {@code registrationDate}, and before {@link SaveOwner}. Derived
 * entirely from the owner's own fields, so it is seed-independent. {@code @Val} yields the built
 * owner so the number is mutated in place and persisted by the save step.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = Math.floorMod(owner.getRegistrationDate().getYear(), 100);
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
