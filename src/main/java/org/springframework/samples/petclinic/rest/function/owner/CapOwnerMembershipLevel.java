package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the owner's {@code membershipLevelCap}: a new owner's {@code membershipLevel} cannot exceed
 * one above the current maximum {@code membershipLevel} among their household members (the other
 * owners sharing this owner's {@link Owner#getHouseholdId() householdId}, per {@link HouseholdMembers}).
 * The cap is one above that maximum; {@link Owner#getMembershipLevel()} applies it. When the owner has
 * no household id or no existing household member, no cap applies and the level stands unchanged.
 *
 * <p>The cap is a transient, response-time value set on the in-flight owner; it is not persisted.
 * Wired into the create pipeline (after {@link AssignOwnerHousehold} has stamped the shared id) and
 * the single-owner GET pipeline, so reads apply the same ceiling.
 */
public class CapOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        List<Owner> members = HouseholdMembers.of(owner, ownerRepository);
        if (members.isEmpty()) {
            return;
        }
        int maxMemberLevel = members.stream().mapToInt(Owner::getMembershipLevel).max().getAsInt();
        owner.setMembershipLevelCap(maxMemberLevel + 1);
    }
}
