package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's {@code membershipPoints} and maps them to a numeric {@code membershipLevel}.
 * Points start at 0 and accrue from the owner's factors: +2 when an email is present, +1 when its
 * {@code namesakeCount} is 0, +2 for a household of 3 or more members, and +3 when tenure — the days
 * from its {@code registrationDate} to today — exceeds 365. The total maps to a level: 1 for 0-1
 * points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
 *
 * <p>The household factor needs the number of owners sharing the household, which only the
 * repository knows; {@link #pointsOf(Owner)} scores the owner's own fields with no household bump,
 * while {@link #pointsOf(Owner, int)} adds it once the caller supplies the household size.
 */
public final class MembershipLevel {

    /** Points awarded when the owner has an email. */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner shares its name with no existing owner. */
    private static final int UNIQUE_NAME_POINTS = 1;

    /** Points awarded when the owner's household has {@value #HOUSEHOLD_THRESHOLD} or more members. */
    private static final int HOUSEHOLD_POINTS = 2;

    /** Points awarded when the owner's tenure exceeds {@value #TENURE_DAYS} days. */
    private static final int TENURE_POINTS = 3;

    /** Household size, in members, at or above which {@link #HOUSEHOLD_POINTS} is awarded. */
    private static final int HOUSEHOLD_THRESHOLD = 3;

    /** Tenure, in days, that an owner must exceed to earn {@link #TENURE_POINTS}. */
    private static final long TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /** Membership points from the owner's own fields, without the household factor. */
    public static int pointsOf(Owner owner) {
        return pointsOf(owner, 0);
    }

    /** Membership points including +2 when {@code householdSize} is 3 or more. */
    public static int pointsOf(Owner owner, int householdSize) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            points += EMAIL_POINTS;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        if (householdSize >= HOUSEHOLD_THRESHOLD) {
            points += HOUSEHOLD_POINTS;
        }
        if (exceedsTenure(owner.getRegistrationDate())) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /** Maps {@code points} to a level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    public static int levelOf(int points) {
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

    /** Whether {@code registrationDate} is more than {@value #TENURE_DAYS} days before today. */
    private static boolean exceedsTenure(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS;
    }
}
