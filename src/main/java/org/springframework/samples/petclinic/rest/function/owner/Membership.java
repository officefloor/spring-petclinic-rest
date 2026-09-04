package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Response-time membership scoring: the entity-derivable {@link MembershipPoints} plus the
 * household factor (+2 for a household of 3 or more), which needs the repository to count
 * members. Sets both {@code membershipPoints} and the {@link MembershipLevel} mapped from them.
 * Mirrors the response-time derivation used for {@link PossibleDuplicate} and {@link BulkSignup}.
 */
public final class Membership {

    private static final int HOUSEHOLD_POINTS = 2;

    private static final int HOUSEHOLD_THRESHOLD = 3;

    private Membership() {
    }

    public static void mark(OwnerDto dto, Owner owner, OwnerRepository ownerRepository) {
        int points = MembershipPoints.of(owner) + householdPoints(owner, ownerRepository);
        dto.setMembershipPoints(points);
        dto.setMembershipLevel(HouseholdCeiling.cap(owner, MembershipLevel.of(points), ownerRepository));
    }

    private static int householdPoints(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        long members = ownerRepository.findAll().stream()
            .filter(other -> Objects.equals(householdId, other.getHouseholdId()))
            .count();
        return members >= HOUSEHOLD_THRESHOLD ? HOUSEHOLD_POINTS : 0;
    }
}
