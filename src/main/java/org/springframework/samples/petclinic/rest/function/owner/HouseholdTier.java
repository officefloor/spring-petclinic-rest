package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Upgrades an owner's membership tier to {@code GOLD} when their household (owners sharing the same
 * {@code householdId}) has three or more members. Applied after the base BRONZE/SILVER tier has been
 * mapped, so GOLD takes precedence; owners without a household are left untouched.
 */
final class HouseholdTier {

    private HouseholdTier() {
    }

    static void applyGold(Owner owner, OwnerRepository ownerRepository, OwnerDto dto) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        int members = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                members++;
            }
        }
        if (members >= 3) {
            dto.setMembershipLevel(3);
        }
    }
}
