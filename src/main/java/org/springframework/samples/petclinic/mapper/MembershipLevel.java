package org.springframework.samples.petclinic.mapper;

/**
 * Maps a membership points score to the owner's membership level: 1 for 0-1 points,
 * 2 for 2-3, 3 for 4-5 and 4 for 6 or more. The points are computed by
 * {@link MembershipPoints}.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

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
