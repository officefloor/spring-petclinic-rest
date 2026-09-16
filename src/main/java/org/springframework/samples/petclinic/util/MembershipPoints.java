package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership points. The total starts at 0 and gains, independently:
 * {@link #EMAIL_POINTS} when the owner has an email address, {@link #UNIQUE_NAME_POINTS}
 * when the owner has no namesakes ({@code namesakeCount == 0}), {@link #HOUSEHOLD_POINTS}
 * for a household of {@link #HOUSEHOLD_MIN} or more members, and {@link #TENURE_POINTS}
 * once the owner's tenure — measured from its registration date — exceeds
 * {@link #TENURE_DAYS} days. The score maps to a level via {@link MembershipLevel}.
 */
public final class MembershipPoints {

    /** Points gained when the owner has an email address. */
    public static final int EMAIL_POINTS = 2;

    /** Points gained when the owner has no namesakes ({@code namesakeCount == 0}). */
    public static final int UNIQUE_NAME_POINTS = 1;

    /** Points gained for a household of {@link #HOUSEHOLD_MIN} or more members. */
    public static final int HOUSEHOLD_POINTS = 2;

    /** Household size that earns {@link #HOUSEHOLD_POINTS}. */
    public static final int HOUSEHOLD_MIN = 3;

    /** Points gained once tenure exceeds {@link #TENURE_DAYS} days. */
    public static final int TENURE_POINTS = 3;

    /** Days of tenure an owner must exceed to earn {@link #TENURE_POINTS}. */
    public static final int TENURE_DAYS = 365;

    private MembershipPoints() {
    }

    /** The membership points for the given owner as of today. */
    public static int of(Owner owner) {
        return of(owner, LocalDate.now());
    }

    /** The membership points for the given owner, with tenure measured as of {@code asOf}. */
    public static int of(Owner owner, LocalDate asOf) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_MIN) {
            points += HOUSEHOLD_POINTS;
        }
        if (hasTenure(owner.getRegistrationDate(), asOf)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /** Whether the tenure from {@code registrationDate} to {@code asOf} exceeds
     *  {@link #TENURE_DAYS}; false when either date is absent. */
    private static boolean hasTenure(LocalDate registrationDate, LocalDate asOf) {
        if (registrationDate == null || asOf == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, asOf) > TENURE_DAYS;
    }
}
