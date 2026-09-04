package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives the owner's membership level: a number from 1 to 4. It starts at 1, gains 1
 * when an email is on file, gains 1 when the owner had no namesakes at creation
 * (namesakeCount is 0), and gains 1 when the owner's tenure exceeds 365 days. A newly
 * created owner has zero tenure, so a new owner never exceeds level 3.
 */
public final class MembershipLevel {

    private static final int MAX = 4;

    private static final long TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    public static int of(Integer namesakeCount, String email, LocalDate registrationDate) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS) {
            level++;
        }
        return Math.min(level, MAX);
    }
}
