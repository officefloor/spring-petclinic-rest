package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership standing from stable owner fields as a points score and
 * the numeric level (1 to 4) that score maps to, replacing the former rules that computed
 * the level directly.
 *
 * <p>Points start at 0 and accrue independently: {@link #POINTS_EMAIL} (2) when the owner
 * has an email, {@link #POINTS_NO_NAMESAKE} (1) when the owner's {@code namesakeCount} is
 * 0, {@link #POINTS_HOUSEHOLD} (2) when the owner belongs to a household of three or more
 * members ({@code householdSize} of 3 or more), and {@link #POINTS_TENURE} (3) for a
 * tenure of more than {@link #TENURE_DAYS_FOR_POINTS} days measured from the registration
 * date.
 *
 * <p>The points map to a level: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points and
 * 4 for 6 or more points.
 */
public final class MembershipLevels {

    /** Points gained when the owner has an email. */
    public static final int POINTS_EMAIL = 2;

    /** Points gained when the owner's {@code namesakeCount} is 0. */
    public static final int POINTS_NO_NAMESAKE = 1;

    /** Points gained for a household of three or more members. */
    public static final int POINTS_HOUSEHOLD = 2;

    /** Points gained for a tenure of more than {@link #TENURE_DAYS_FOR_POINTS} days. */
    public static final int POINTS_TENURE = 3;

    /** Tenure points require strictly more than this many days of tenure. */
    public static final int TENURE_DAYS_FOR_POINTS = 365;

    private MembershipLevels() {
    }

    /**
     * Compute the membership points for the given owner.
     */
    public static int points(Owner owner) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            points += POINTS_EMAIL;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += POINTS_NO_NAMESAKE;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            points += POINTS_HOUSEHOLD;
        }
        if (hasTenurePoints(owner)) {
            points += POINTS_TENURE;
        }
        return points;
    }

    /**
     * Compute the membership level for the given owner from its points.
     */
    public static int of(Owner owner) {
        int points = points(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    private static boolean hasTenurePoints(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS_FOR_POINTS;
    }
}
