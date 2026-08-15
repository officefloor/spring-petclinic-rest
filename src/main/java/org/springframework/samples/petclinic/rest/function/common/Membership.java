package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's numeric {@code membershipLevel}.
 *
 * <p>The level starts at 1, gains 1 when the owner has an email address on record, gains a
 * further 1 when the owner had no namesakes at creation ({@code namesakeCount} is 0), and gains
 * a fourth when the owner's tenure exceeds 365 days. Level 4 therefore requires tenure of more
 * than 365 days: because a newly created owner has zero tenure, a new owner never exceeds level 3
 * (even one with an email, a {@code namesakeCount} of 0 and a large household is level 3, not 4).
 */
public final class Membership {

    /** Tenure, in days, that must be exceeded before the fourth membership level is granted. */
    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private Membership() {
    }

    /** The owner's numeric membership level, from 1 to 4. */
    public static int levelOf(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (tenureDays(owner) > TENURE_DAYS_FOR_LEVEL_4) {
            level++;
        }
        return Math.min(level, 4);
    }

    /** Days elapsed since the owner's registration date; 0 when no date is on record. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
