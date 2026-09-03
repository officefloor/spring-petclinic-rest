package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Household membership tier. An owner is {@code GOLD} when three or more owners share its
 * {@code householdId}; the SILVER and BRONZE rules (in {@code OwnerMapper}) apply otherwise.
 */
public final class HouseholdTier {

    private HouseholdTier() {
    }

    /** True when the owner belongs to a household of three or more members. */
    public static boolean isGold(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return false;
        }
        int members = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (householdId.equals(other.getHouseholdId())) {
                members++;
            }
        }
        return members >= 3;
    }
}
