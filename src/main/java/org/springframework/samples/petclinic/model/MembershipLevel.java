package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level from their {@link MembershipPoints}.
 *
 * <p>Maps points to a level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, 4 for 6 or more.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership level for the given owner. */
    public static int of(Owner owner) {
        return fromPoints(MembershipPoints.of(owner));
    }

    /** Maps membership points to a membership level. */
    public static int fromPoints(int points) {
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
