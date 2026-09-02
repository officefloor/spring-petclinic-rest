package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Maps an owner's {@link MembershipPoints membership points} to a numeric level from 1 to 4:
 * 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(int membershipPoints) {
        if (membershipPoints >= 6) {
            return 4;
        }
        if (membershipPoints >= 4) {
            return 3;
        }
        if (membershipPoints >= 2) {
            return 2;
        }
        return 1;
    }
}
