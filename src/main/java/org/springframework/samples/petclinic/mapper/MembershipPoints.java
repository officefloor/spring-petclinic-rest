package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Scores an owner's membership points: 2 when an email is on file, 1 when the owner had
 * no namesakes at creation (namesakeCount is 0), 2 for a household of three or more, and
 * 3 when the owner's tenure exceeds 365 days. {@link MembershipLevel} maps the total to a
 * level.
 */
public final class MembershipPoints {

    private static final long TENURE_DAYS = 365;

    private MembershipPoints() {
    }

    public static int of(Integer namesakeCount, String email, LocalDate registrationDate,
            boolean goldHousehold) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (goldHousehold) {
            points += 2;
        }
        if (registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS) {
            points += 3;
        }
        return points;
    }
}
