package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize} on create: the number of owners sharing the same
 * {@code householdId} once this owner is counted. Runs after {@link AssignHousehold} (which sets
 * the {@code householdId} on this owner and back-fills existing household members), so the shared
 * identifier is in place before the count.
 *
 * <p>The new owner is not yet saved, so it is counted by adding one to the existing same-household
 * members. An owner with no {@code householdId} (did not opt into a shared household) is a
 * household of one.
 *
 * <p>Consumed by {@link org.springframework.samples.petclinic.mapper.OwnerMapper}, which derives
 * the {@code GOLD} membership tier when a household reaches three or more members.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int existing = (int) ownerRepository.findAll().stream()
                .filter(existingOwner -> existingOwner.getId() == null
                        || !existingOwner.getId().equals(owner.getId()))
                .filter(existingOwner -> householdId.equals(existingOwner.getHouseholdId()))
                .count();
        owner.setHouseholdSize(existing + 1);
    }
}
