package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level from their {@link MembershipPoints}.
 *
 * <p>Maps points to a level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, 4 for 6 or more.
 *
 * <p>The points-derived level is then bounded by the owner's household level ceiling, if
 * one was set at creation (see {@code AssignMembershipLevelCap}): a new owner's level
 * cannot exceed one above the maximum level among their existing household members. When
 * no ceiling was recorded (the owner had no existing household member) the level is
 * uncapped.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership level for the given owner, bounded by the household ceiling if set. */
    public static int of(Owner owner) {
        int level = fromPoints(MembershipPoints.of(owner));
        Integer cap = owner.getMembershipLevelCap();
        return cap == null ? level : Math.min(level, cap);
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
