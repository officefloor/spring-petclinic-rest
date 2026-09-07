package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Numeric membership level for pet owners, from 1 to 4, derived from an owner's
 * {@link MembershipPoints}. The points are banded into a level: 1 for 0-1 points, 2 for 2-3 points,
 * 3 for 4-5 points and 4 for 6 or more points. Because a newly created owner earns no tenure points,
 * a new owner tops out at 5 points (email, unique name and a household of 3 or more), reaching level
 * 4 only once its tenure passes 365 days. Used by the owner mapper to expose {@code membershipLevel}
 * on responses.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The membership level for the given points total: 1 for 0-1, 2 for 2-3, 3 for 4-5, 4 for 6 or
     * more.
     */
    public static int of(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }
}
