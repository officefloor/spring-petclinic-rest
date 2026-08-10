package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's membership points and the numeric membership level bucketed from them.
 *
 * <p>Points start at {@code 0} and accumulate: {@code +2} when an email address is present;
 * {@code +1} when the owner has no namesakes ({@code namesakeCount} is {@code 0}); {@code +2} for a
 * household of {@code 3} or more members; {@code +3} for tenure of more than {@code 365} days — the
 * number of days between the owner's {@code registrationDate} and today.
 *
 * <p>Points map to a level of {@code 1} ({@code 0}-{@code 1} points), {@code 2} ({@code 2}-{@code 3}),
 * {@code 3} ({@code 4}-{@code 5}) or {@code 4} ({@code 6} or more).
 */
public final class MembershipLevel {

    /** Tenure, in days, that must be exceeded to earn the tenure points. */
    private static final long TENURE_DAYS_FOR_BONUS = 365;

    /** Household size, in members, that must be reached to earn the household points. */
    private static final int HOUSEHOLD_SIZE_FOR_BONUS = 3;

    private MembershipLevel() {
    }

    /** Compute the membership points for the given owner. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null
                && owner.getHouseholdMemberCount() >= HOUSEHOLD_SIZE_FOR_BONUS) {
            points += 2;
        }
        if (tenureDays(owner) > TENURE_DAYS_FOR_BONUS) {
            points += 3;
        }
        return points;
    }

    /** Bucket a points total into a membership level (1..4). */
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

    /** Compute the membership level (1..4) for the given owner. */
    public static int of(Owner owner) {
        return levelForPoints(points(owner));
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
