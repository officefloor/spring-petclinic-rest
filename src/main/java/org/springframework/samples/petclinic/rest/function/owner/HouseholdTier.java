package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Adds the household membership-points factor: +2 points when the owner's household (owners sharing
 * the same {@code householdId}) has three or more members. Applied after the owner-only points have
 * been mapped, then the membership level is recomputed; owners without a household are left untouched.
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
            int points = dto.getMembershipPoints() + 2;
            dto.setMembershipPoints(points);
            dto.setMembershipLevel(MembershipLevel.level(points));
        }
        // A new member's level may sit at most one above the current household maximum.
        int bonus = members >= 3 ? 2 : 0;
        int maxOther = -1;
        for (Owner existing : ownerRepository.findAll()) {
            if (!householdId.equals(existing.getHouseholdId())
                    || (existing.getId() != null && existing.getId().equals(owner.getId()))) {
                continue;
            }
            maxOther = Math.max(maxOther, MembershipLevel.level(MembershipLevel.points(existing) + bonus));
        }
        if (maxOther >= 0 && dto.getMembershipLevel() > maxOther + 1) {
            dto.setMembershipLevel(maxOther + 1);
        }
    }
}
