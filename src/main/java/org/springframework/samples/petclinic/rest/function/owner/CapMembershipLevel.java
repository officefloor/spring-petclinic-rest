package org.springframework.samples.petclinic.rest.function.owner;

import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps a newly scored owner's {@code membershipLevel} at one above the current maximum level among the
 * existing members of its household, so a new member cannot leapfrog their household's standing. Runs
 * after {@link AssignMembership} has scored the owner and keys off the same existing household members
 * as {@link CountHouseholdMembers}. With no existing household member (a new or single-owner household)
 * no ceiling applies and the assigned level stands.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        OptionalInt maxLevel = Household.existingMembers(owner, ownerRepository).stream()
            .map(Owner::getMembershipLevel)
            .filter(level -> level != null)
            .mapToInt(Integer::intValue)
            .max();
        if (maxLevel.isEmpty()) {
            return;
        }
        int ceiling = maxLevel.getAsInt() + 1;
        if (owner.getMembershipLevel() > ceiling) {
            owner.setMembershipLevel(ceiling);
        }
    }
}
