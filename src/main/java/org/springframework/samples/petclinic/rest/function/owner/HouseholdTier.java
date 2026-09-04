package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Household tier rule: an owner is promoted to the {@code GOLD} membership tier once its household
 * — the owners sharing its {@code householdId} — has {@value #GOLD_THRESHOLD} or more members,
 * overriding the BRONZE/SILVER tier the mapper derives from the owner alone.
 */
public final class HouseholdTier {

    private static final int GOLD_THRESHOLD = 3;

    private HouseholdTier() {
    }

    public static void applyTo(OwnerDto dto, Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        long members = ownerRepository.findAll().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count();
        if (members >= GOLD_THRESHOLD) {
            dto.setMembershipTier("GOLD");
        }
    }
}
