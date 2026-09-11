package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric membership points.
 *
 * <p>Starts at 0; add 2 when an email is present; add 1 when {@code namesakeCount}
 * is 0; add 2 for a household of 3 or more; add 3 for tenure over 365 days.
 */
public final class MembershipPoints {

    /** Household size, at or above which the household bonus applies. */
    public static final int HOUSEHOLD_SIZE = 3;

    /** Tenure, in days, that must be exceeded to earn the tenure bonus. */
    public static final long TENURE_DAYS = 365;

    private MembershipPoints() {
    }

    /** The membership points for the given owner. */
    public static int of(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_SIZE) {
            points += 2;
        }
        if (hasTenure(owner)) {
            points += 3;
        }
        return points;
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
