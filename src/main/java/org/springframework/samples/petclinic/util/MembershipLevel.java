package org.springframework.samples.petclinic.util;

/**
 * Bands {@link MembershipPoints membership points} into a membership level, a number from 1 to 4:
 * level 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more points. Because a newly created
 * owner has zero tenure, a new owner never earns the tenure points and so never exceeds level 3.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership level for {@code points}, or {@code null} when {@code points} is {@code null}. */
    public static Integer forPoints(Integer points) {
        if (points == null) {
            return null;
        }
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
