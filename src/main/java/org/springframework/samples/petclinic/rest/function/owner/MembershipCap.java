package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ceiling on an owner's {@code membershipLevel}: it may sit at most one above the current
 * maximum level among their household members (other owners sharing the same
 * {@link HouseholdId}). With no existing household member no cap applies, so a sole owner
 * keeps their raw {@link MembershipTier} level.
 */
public final class MembershipCap {

    private MembershipCap() {
    }

    public static int level(Owner owner, int rawLevel, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        Integer maxMemberLevel = null;
        for (Owner member : ownerRepository.findAll()) {
            if (member.getId() != null && member.getId().equals(owner.getId())) {
                continue;
            }
            if (!householdId.equals(HouseholdId.of(member.getLastName(), member.getPostcode()))) {
                continue;
            }
            int memberLevel = MembershipTier.level(MembershipTier.points(
                member.getNamesakeCount(), member.getEmail(), member.getRegistrationDate()));
            if (maxMemberLevel == null || memberLevel > maxMemberLevel) {
                maxMemberLevel = memberLevel;
            }
        }
        return maxMemberLevel == null ? rawLevel : Math.min(rawLevel, maxMemberLevel + 1);
    }
}
