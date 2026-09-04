package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.owner.HouseholdTier;

/**
 * Caps a new owner's membership level at one above the highest level among the household
 * members that already existed when they were created (same household — see
 * {@link HouseholdId} — and a lower, i.e. earlier, id). With no earlier household member
 * no cap applies and the raw level stands.
 */
public final class HouseholdCap {

    private HouseholdCap() {
    }

    public static int cap(Owner owner, int rawLevel, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        int ceiling = -1;
        for (Owner other : ownerRepository.findAll()) {
            if (isEarlierMember(owner, other, householdId)) {
                ceiling = Math.max(ceiling, cap(other, rawLevel(other, ownerRepository), ownerRepository));
            }
        }
        return ceiling < 0 ? rawLevel : Math.min(rawLevel, ceiling + 1);
    }

    private static boolean isEarlierMember(Owner owner, Owner other, String householdId) {
        return owner.getId() != null && other.getId() != null && other.getId() < owner.getId()
                && householdId.equals(HouseholdId.of(other.getLastName(), other.getPostcode()));
    }

    private static int rawLevel(Owner owner, OwnerRepository ownerRepository) {
        int points = MembershipPoints.of(owner.getNamesakeCount(), owner.getEmail(),
                owner.getRegistrationDate(), HouseholdTier.isGold(owner, ownerRepository));
        return MembershipLevel.of(points);
    }
}
