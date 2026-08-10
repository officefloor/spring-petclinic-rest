package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's membership standing from a points score. Points start at 0 and accrue: +2 for a
 * usable email address, +1 when the owner has no namesakes, +2 for a household of three or more, and
 * +3 for a tenure of more than 365 days measured from the registration date. The points are mapped to
 * a membership level of 1 to 4: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more. Because a
 * newly created owner has zero tenure, a new owner scores at most 5 points (level 3).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** Returns the membership points for an owner with the given {@code email}, {@code namesakeCount},
     *  {@code householdSize} and {@code registrationDate}, as at {@code today}. */
    public static int points(String email, Integer namesakeCount, Integer householdSize,
            LocalDate registrationDate, LocalDate today) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (registrationDate != null && today != null
            && ChronoUnit.DAYS.between(registrationDate, today) > 365) {
            points += 3;
        }
        return points;
    }

    /** Maps membership {@code points} to a level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
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
