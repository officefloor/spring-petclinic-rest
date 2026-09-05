package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives the membership scoring for an owner. {@link #points} starts at 0 and adds 2
 * when an email address is present, 1 when namesakeCount is 0, 2 for a household of 3 or
 * more (the new owner plus namesakeCount existing members) and 3 for tenure over 365 days
 * (days between registrationDate and today). {@link #level} maps those points to 1 (0-1),
 * 2 (2-3), 3 (4-5) or 4 (6 or more).
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    private static boolean hasTenure(LocalDate registrationDate) {
        return registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365;
    }

    public static int points(Integer namesakeCount, String email, LocalDate registrationDate) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (namesakeCount != null && namesakeCount >= 2) {
            points += 2;
        }
        if (hasTenure(registrationDate)) {
            points += 3;
        }
        return points;
    }

    public static int level(int points) {
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
