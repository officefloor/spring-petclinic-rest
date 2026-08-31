package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership points and the level they map to. Points start at 0 and gain +2 for a
 * present email, +1 when the owner has no namesake, +2 for a household of 3 or more and
 * +3 for tenure over 365 days; the total maps to a level of 1 (0-1), 2 (2-3), 3 (4-5)
 * or 4 (6 or more).
 */
public final class Membership {

    private Membership() {
    }

    /** Total membership points for {@code owner} given its household and tenure flags. */
    public static int points(Owner owner, boolean largeHousehold, boolean longTenure) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (largeHousehold) {
            points += 2;
        }
        if (longTenure) {
            points += 3;
        }
        return points;
    }

    /** The membership level that {@code points} maps to: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6+). */
    public static int level(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }
}
