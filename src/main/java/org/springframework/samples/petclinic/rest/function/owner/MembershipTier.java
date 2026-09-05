package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives the numeric {@code membershipLevel} (1-4): starts at 1, adds 1 when an email
 * address is present, adds 1 when namesakeCount is 0, and adds 1 for tenure of more than
 * 365 days (days between registrationDate and today). A newly created owner has zero
 * tenure, so it never exceeds level 3.
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    private static boolean hasTenure(LocalDate registrationDate) {
        return registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365;
    }

    public static int of(Integer namesakeCount, String email, LocalDate registrationDate) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (hasTenure(registrationDate)) {
            level++;
        }
        return Math.min(level, 4);
    }
}
