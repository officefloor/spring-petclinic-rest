package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership points. The score starts at 0 and gains {@link #EMAIL_POINTS} when an
 * email address is present, {@link #UNIQUE_NAME_POINTS} when the owner has a unique name
 * (namesakeCount is zero), {@link #HOUSEHOLD_POINTS} for a household of {@link #HOUSEHOLD_MIN} or more
 * members, and {@link #TENURE_POINTS} when the owner's {@link Tenure tenure} exceeds
 * {@link #TENURE_DAYS} days. Because a newly created owner has zero tenure, a new owner never earns
 * the tenure points. The points band into a {@link MembershipLevel membership level}.
 */
public final class MembershipPoints {

    /** Points awarded when an email address is present. */
    public static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has a unique name (namesakeCount is zero). */
    public static final int UNIQUE_NAME_POINTS = 1;

    /** Points awarded for a household of {@link #HOUSEHOLD_MIN} or more members. */
    public static final int HOUSEHOLD_POINTS = 2;

    /** Points awarded when tenure exceeds {@link #TENURE_DAYS} days. */
    public static final int TENURE_POINTS = 3;

    /** Household size at or above which the household points are awarded. */
    public static final int HOUSEHOLD_MIN = 3;

    /** Tenure in days beyond which the tenure points are awarded. */
    public static final int TENURE_DAYS = 365;

    private MembershipPoints() {
    }

    /** The membership points for {@code owner}, or {@code null} when the owner is {@code null}. */
    public static Integer of(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= HOUSEHOLD_MIN) {
            points += HOUSEHOLD_POINTS;
        }
        if (Tenure.days(owner) > TENURE_DAYS) {
            points += TENURE_POINTS;
        }
        return points;
    }
}
