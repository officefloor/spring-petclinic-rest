package org.springframework.samples.petclinic.mapper;

/**
 * Derives the owner's membership level: a number from 1 to 3 fixed at creation. It
 * starts at 1, gains 1 when an email is on file, gains 1 when the owner had no
 * namesakes at creation (namesakeCount is 0), and is capped at 3 (level 4 is reserved
 * for tenure). The value is a pure function of those two fields, so it needs no stored
 * state.
 */
public final class MembershipLevel {

    private static final int MAX = 3;

    private MembershipLevel() {
    }

    public static int of(Integer namesakeCount, String email) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, MAX);
    }
}
