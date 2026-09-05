package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the numeric {@code membershipLevel} (1-3) assigned on creation: starts at 1,
 * adds 1 when an email address is present, adds 1 when namesakeCount is 0, capped at 3
 * (level 4 is reserved for tenure).
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    public static int of(Integer namesakeCount, String email) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}
