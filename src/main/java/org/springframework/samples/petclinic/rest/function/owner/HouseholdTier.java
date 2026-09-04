package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.HouseholdId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner's household qualifies for the {@code GOLD} membership tier:
 * true once three or more owners share this owner's householdId (same last name and
 * address). Otherwise the {@code SILVER}/{@code BRONZE} rules in
 * {@link org.springframework.samples.petclinic.mapper.MembershipTier} apply.
 */
public final class HouseholdTier {

    private static final int GOLD_THRESHOLD = 3;

    private HouseholdTier() {
    }

    public static boolean isGold(Owner owner, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner.getLastName(), owner.getAddress());
        long members = ownerRepository.findAll().stream()
                .filter(o -> householdId.equals(HouseholdId.of(o.getLastName(), o.getAddress())))
                .count();
        return members >= GOLD_THRESHOLD;
    }
}
