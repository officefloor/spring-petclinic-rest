package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link AssignCustomerCode} and
 * {@link DefaultOwnerRegistrationDate}, before {@link SaveOwner}. Assigns the owner's
 * {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'} where YY is the last two digits
 * of the registrationDate year over the region-and-hash customerCode (e.g. 'NSW-1A2B3C4D-M26').
 * Mutates the built owner in place so later
 * steps store and respond with the assigned number.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
