package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner's response should carry the {@code GOLD} membership tier: it is raised
 * once the owner's household (the owners sharing its {@code householdId}, see
 * {@link AssignHousehold}) has three or more members, this owner included. The count is by the
 * stored {@code householdId}, so the create response for the member that completes a three-member
 * household and later reads of that owner agree. When GOLD does not apply the existing SILVER and
 * BRONZE rules stand (see {@link org.springframework.samples.petclinic.mapper.OwnerMapper}).
 */
final class HouseholdMembership {

    /** Raise GOLD once a household reaches this many members. */
    private static final long GOLD_AT = 3;

    private HouseholdMembership() {
    }

    /** True when the owner belongs to a household of three or more members. */
    static boolean isGold(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return false;
        }
        long members = ownerRepository.findAll().stream()
                .filter(existing -> householdId.equals(existing.getHouseholdId()))
                .count();
        return members >= GOLD_AT;
    }
}
