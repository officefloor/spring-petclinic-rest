package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric membership level.
 *
 * <p>Starts at 1; add 1 when an email is present; add 1 when {@code namesakeCount}
 * is 0; these pre-tenure factors are capped at 3. Level 4 is reserved for tenure:
 * it requires a {@code registrationDate} more than 365 days in the past. Because a
 * newly created owner has zero tenure, a new owner never exceeds level 3.
 */
public final class MembershipLevel {

    /** The maximum level from pre-tenure factors; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    /** Tenure, in days, that must be exceeded to reach level 4. */
    public static final long TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /** The membership level for the given owner. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, MAX);
        if (hasTenure(owner)) {
            level++;
        }
        return level;
    }

    /** Whether the owner's tenure exceeds {@link #TENURE_DAYS} days. */
    private static boolean hasTenure(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS;
    }
}
