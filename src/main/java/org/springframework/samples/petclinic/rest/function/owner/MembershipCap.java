package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Caps an owner's {@code membershipLevel} against its household.
 *
 * <p>A new owner's level cannot exceed one above the current maximum {@code membershipLevel} among
 * the other members of its household (owners sharing its {@link Owner#getHouseholdId() householdId},
 * itself excluded). With no existing household member the owner's own level stands — no cap applies.
 *
 * <p>Because {@code membershipLevel} is derived, not stored, the cap is recomputed wherever the
 * final per-owner level is produced ({@link RespondWithOwner}, {@link RespondWithOwnerCreated} and
 * {@link AuditOwnerCreated}). Each household member's level is scored with the same household size,
 * so the ceiling is deterministic and independent of which member is being viewed.
 */
final class MembershipCap {

    private MembershipCap() {
    }

    /**
     * The {@code owner}'s level, capped at one above the highest level among the other owners in its
     * household. Returns {@code ownLevel} unchanged when the owner has no household id or no other
     * household member exists.
     */
    static int cappedLevelFor(Owner owner, int ownLevel, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return ownLevel;
        }
        Integer id = owner.getId();
        int householdSize = Household.memberCount(owner, ownerRepository);
        Integer maxOtherLevel = null;
        for (Owner other : ownerRepository.findAll()) {
            if (!householdId.equals(other.getHouseholdId())) {
                continue;
            }
            if (id != null && id.equals(other.getId())) {
                continue; // the owner itself is not one of its own household members
            }
            int otherLevel = MembershipLevel.levelOf(MembershipLevel.pointsOf(other, householdSize));
            if (maxOtherLevel == null || otherLevel > maxOtherLevel) {
                maxOtherLevel = otherLevel;
            }
        }
        if (maxOtherLevel == null) {
            return ownLevel; // no existing household member -> no cap
        }
        return Math.min(ownLevel, maxOtherLevel + 1);
    }
}
