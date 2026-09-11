package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric {@code membershipLevel} (1-4) from its attributes:
 * start at 1, add 1 when an email is present, add 1 when {@code namesakeCount} is
 * 0, capped at 3 for these create-time factors. Level 4 requires tenure of more
 * than 365 days, so a brand-new owner (zero tenure) never exceeds level 3.
 */
public final class MembershipLevels {

    /** Tenure, in days, above which an owner reaches level 4. */
    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private MembershipLevels() {
    }

    /** The membership level (1-4) for the given owner. */
    public static int forOwner(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, 3);
        if (tenureInDays(owner) > TENURE_DAYS_FOR_LEVEL_4) {
            level++;
        }
        return level;
    }

    /** Days since registration, or 0 when the registration date is unknown. */
    private static long tenureInDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
