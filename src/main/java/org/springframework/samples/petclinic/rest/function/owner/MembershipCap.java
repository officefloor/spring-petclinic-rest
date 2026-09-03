package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ceilings an owner's membership level to one above the current maximum {@link
 * MembershipLevel} among their existing household members — the other live owners resolving
 * to the same {@link HouseholdId}. With no existing household member the owner's own level
 * stands, so a household's first member is never capped.
 */
public final class MembershipCap {

    private MembershipCap() {
    }

    public static int apply(Owner owner, int level, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner);
        int ceiling = ownerRepository.findAll().stream()
                .filter(other -> !other.isDeleted())
                .filter(other -> other.getId() != null && !other.getId().equals(owner.getId()))
                .filter(other -> householdId.equals(HouseholdId.of(other)))
                .mapToInt(other -> MembershipLevel.level(MembershipLevel.points(other,
                        Namesakes.countBefore(other, ownerRepository), Household.size(other, ownerRepository))))
                .max()
                .orElse(-1);
        return ceiling < 0 ? level : Math.min(level, ceiling + 1);
    }
}
