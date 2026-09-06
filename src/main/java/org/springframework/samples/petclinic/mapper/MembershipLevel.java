package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's membership standing from the state captured when the owner
 * was created together with how long the owner has been registered. Kept as a
 * plain static helper - rather than a method on {@link OwnerMapper} - so
 * MapStruct does not mistake it for an implicit property mapping.
 *
 * <p>Standing is scored as points, starting at 0: an email address is worth 2,
 * a name that was unique on create ({@code namesakeCount} is 0) is worth 1, a
 * household of 3 or more members is worth 2, and tenure exceeding
 * {@link #TENURE_DAYS_FOR_BONUS} days is worth 3. Those points map to a numeric
 * membership level: 1 (0-1 points), 2 (2-3 points), 3 (4-5 points) and 4 (6 or
 * more points).
 */
public final class MembershipLevel {

    /** Points awarded when an email address is present. */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the name was unique on create (namesakeCount is 0). */
    private static final int UNIQUE_NAME_POINTS = 1;

    /** Points awarded for a household of 3 or more members. */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Points awarded when tenure exceeds {@link #TENURE_DAYS_FOR_BONUS} days. */
    private static final int TENURE_POINTS = 3;

    /** Household size, in members, at or above which the household bonus applies. */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Tenure, in days, that must be exceeded to earn the tenure bonus. */
    private static final int TENURE_DAYS_FOR_BONUS = 365;

    private MembershipLevel() {
    }

    /**
     * Computes the owner's membership points: 0, plus {@value #EMAIL_POINTS} when
     * an email address is present, plus {@value #UNIQUE_NAME_POINTS} when
     * {@code namesakeCount} is 0, plus {@value #LARGE_HOUSEHOLD_POINTS} for a
     * household of {@value #LARGE_HOUSEHOLD_SIZE} or more members, plus
     * {@value #TENURE_POINTS} when the owner's tenure exceeds
     * {@link #TENURE_DAYS_FOR_BONUS} days.
     *
     * @param owner the owner whose points to derive
     * @return the owner's membership points
     */
    public static int pointsForOwner(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureExceedsBonusThreshold(owner.getRegistrationDate())) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Maps the owner's membership points to a numeric membership level: 1 for
     * 0-1 points, 2 for 2-3 points, 3 for 4-5 points and 4 for 6 or more points.
     *
     * @param owner the owner whose level to derive
     * @return the numeric membership level between 1 and 4 inclusive
     */
    public static int forOwner(Owner owner) {
        return levelForPoints(pointsForOwner(owner));
    }

    /**
     * Maps a membership-points total to its membership level: 1 (0-1), 2 (2-3),
     * 3 (4-5), 4 (6 or more).
     *
     * @param points the membership points
     * @return the corresponding membership level between 1 and 4 inclusive
     */
    public static int levelForPoints(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    /**
     * Returns whether the tenure elapsed since {@code registrationDate} is more
     * than {@link #TENURE_DAYS_FOR_BONUS} days. A {@code null} or future
     * registration date yields zero (or negative) tenure and therefore never
     * qualifies.
     *
     * @param registrationDate the day the owner was registered, or {@code null}
     * @return {@code true} when tenure exceeds the tenure-bonus threshold
     */
    private static boolean tenureExceedsBonusThreshold(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        long tenureDays = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return tenureDays > TENURE_DAYS_FOR_BONUS;
    }

}
