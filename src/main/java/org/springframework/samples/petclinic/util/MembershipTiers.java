package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code membershipTier} from the owner's current state.
 *
 * <ul>
 *   <li>{@code GOLD} — the owner's household (owners sharing the same {@code householdId}) has 3 or
 *       more members, as stamped onto {@link Owner#getHouseholdMemberCount()} at response time.</li>
 *   <li>{@code SILVER} — not GOLD, the owner has no namesakes ({@code namesakeCount == 0}) and a
 *       non-blank email.</li>
 *   <li>{@code BRONZE} — otherwise.</li>
 * </ul>
 */
public final class MembershipTiers {

    private MembershipTiers() {
    }

    public static String tierFor(Owner owner) {
        Integer members = owner.getHouseholdMemberCount();
        if (members != null && members >= 3) {
            return "GOLD";
        }
        boolean silver = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0
                && owner.getEmail() != null && !owner.getEmail().isBlank();
        return silver ? "SILVER" : "BRONZE";
    }
}
