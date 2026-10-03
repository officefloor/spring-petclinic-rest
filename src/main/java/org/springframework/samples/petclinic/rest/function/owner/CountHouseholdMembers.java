package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize} on create: the number of owners sharing the same
 * household once this owner is counted. The household is keyed on the deterministic
 * {@code householdId} (normalized last name and postcode — see {@link HouseholdKey}), so a member
 * is any other owner that computes the same id.
 *
 * <p>The new owner is not yet saved, so it is counted by adding one to the existing same-household
 * members. A fully-unique owner (no other owner shares its last name and postcode) is therefore a
 * household of one.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        HouseholdKey household = HouseholdKey.of(owner);
        int existing = (int) ownerRepository.findAll().stream()
                .filter(existingOwner -> existingOwner.getId() == null
                        || !existingOwner.getId().equals(owner.getId()))
                .filter(household::matches)
                .count();
        owner.setHouseholdSize(existing + 1);
    }
}
