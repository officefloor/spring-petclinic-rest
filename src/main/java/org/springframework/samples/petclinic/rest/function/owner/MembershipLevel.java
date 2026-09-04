package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Membership level rule: maps a {@link MembershipPoints} total to a level of 1 (0-1 points),
 * 2 (2-3), 3 (4-5) or 4 (6 or more). Owns only that mapping; the factors that earn points, and
 * their weights, live in the small factor classes behind {@link MembershipPoints}.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(int points) {
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
