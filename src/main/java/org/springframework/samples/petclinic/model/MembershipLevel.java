package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric membership level.
 *
 * <p>Starts at {@code 1}; adds {@code 1} when an email address is present; adds {@code 1} when the
 * owner has no namesakes ({@code namesakeCount} is {@code 0}); these pre-tenure factors are capped
 * at {@code 3}. Level {@code 4} requires tenure of more than {@code 365} days — the number of days
 * between the owner's {@code registrationDate} and today — so a brand-new owner (zero tenure) never
 * exceeds level {@code 3}.
 */
public final class MembershipLevel {

    /** Tenure, in days, that must be exceeded to reach level {@code 4}. */
    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private MembershipLevel() {
    }

    /** Compute the membership level (1..4) for the given owner. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, 3);
        if (tenureDays(owner) > TENURE_DAYS_FOR_LEVEL_4) {
            level++;
        }
        return level;
    }

    /** Days between the owner's {@code registrationDate} and today; {@code 0} when unknown. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
