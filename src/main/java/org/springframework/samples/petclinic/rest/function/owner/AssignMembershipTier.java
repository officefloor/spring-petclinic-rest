package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner a membership tier based on creation order. The first 100
 * owners ever created (membership numbers 1 through 100) are 'FOUNDING'; all
 * later owners are 'STANDARD'. Runs after the sequential membership number has
 * been assigned and before the owner is saved.
 */
public class AssignMembershipTier {

    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner) {
        Integer membershipNumber = owner.getMembershipNumber();
        String tier = membershipNumber != null && membershipNumber <= FOUNDING_LIMIT
                ? "FOUNDING" : "STANDARD";
        owner.setMembershipTier(tier);
    }
}
