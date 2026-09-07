package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code householdSize}: the number of
 * members its household ({@link Household#id(String, String) same householdId}, i.e. same last name
 * and address) has once this owner is included. Counts the existing owners sharing the household
 * (before this create) and adds one for the owner being created. Runs before {@link SaveOwner} so
 * the owner being created is not double-counted. The stored value drives the {@code GOLD} membership
 * tier ({@link MembershipTier}) for households of three or more members.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = Household.id(owner.getLastName(), owner.getAddress());
        int existing = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (householdId.equals(Household.id(other.getLastName(), other.getAddress()))) {
                existing++;
            }
        }
        owner.setHouseholdSize(existing + 1);
    }
}
