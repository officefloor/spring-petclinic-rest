package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Numeric membership level for pet owners, from 1 to 4, derived from an owner's
 * {@link MembershipPoints}. The points are banded into a level: 1 for 0-1 points, 2 for 2-3 points,
 * 3 for 4-5 points and 4 for 6 or more points. Because a newly created owner earns no tenure points,
 * a new owner tops out at 5 points (email, unique name and a household of 3 or more), reaching level
 * 4 only once at least one fiscal year has elapsed. Used by the owner mapper to expose {@code membershipLevel}
 * on responses.
 *
 * <p>A new owner's level is additionally capped at creation time so it cannot exceed one above the
 * maximum level then held by its household members (see {@link CapMembershipLevel}); the ceiling is
 * stored on the owner as {@code membershipLevelCap}. {@link #of(Owner)} applies that ceiling to the
 * points-banded level. With no ceiling recorded (no existing household member, or a pre-existing
 * owner) the level is the points-banded value unchanged.
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

    /**
     * The owner's effective membership level: the points-banded level ({@link #of(int)} over
     * {@link MembershipPoints}), lowered to the stored {@code membershipLevelCap} ceiling when one
     * was recorded at create time. With no ceiling the points-banded level is returned unchanged.
     */
    public static int of(Owner owner) {
        int level = of(MembershipPoints.of(owner.getNamesakeCount(), owner.getEmail(),
                owner.getHouseholdSize(), owner.getRegistrationDate()));
        Integer cap = owner.getMembershipLevelCap();
        return cap == null ? level : Math.min(level, cap);
    }
}
