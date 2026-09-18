package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in {@code POST /api/owners} after {@link AssignOwnerMembershipLevel} has derived the owner's
 * membership level from its points and before it is saved, within the create transaction. Caps that
 * level at one above the current maximum membership level among the owner's existing household
 * members (those already sharing its {@link Owner#getHouseholdId() household id}), so a new member
 * cannot leap more than one level beyond the household. With no existing household member no cap
 * applies. Because the new owner has not yet been persisted, {@link OwnerRepository#findAll()}
 * returns only the existing members, so the owner is never capped against itself.
 */
public class CapOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        ownerRepository.findAll().stream()
                .filter(existing -> householdId.equals(existing.getHouseholdId()))
                .map(Owner::getMembershipLevel)
                .filter(level -> level != null)
                .mapToInt(Integer::intValue)
                .max()
                .ifPresent(maxLevel -> owner.setMembershipLevel(
                        Math.min(owner.getMembershipLevel(), maxLevel + 1)));
    }
}
