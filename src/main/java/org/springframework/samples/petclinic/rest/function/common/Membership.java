package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code membershipPoints} and the numeric {@code membershipLevel} mapped from
 * them.
 *
 * <p>Points start at 0 and accumulate: 2 when the owner has an email address on record, 1 when the
 * owner had no namesakes at creation ({@code namesakeCount} is 0), 2 for a household of 3 or more
 * members, and 3 when the owner's tenure exceeds one elapsed fiscal year. The points then map to a
 * level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more. Because a newly created owner
 * has zero tenure, the +3 tenure award is out of reach at creation, so a new owner scores at most 5
 * points (2 email + 1 namesake + 2 household) and never exceeds level 3.
 */
public final class Membership {

    /** Household size, in members, at or above which the household points are granted. */
    private static final int HOUSEHOLD_SIZE_FOR_POINTS = 3;

    /** Tenure, in elapsed fiscal years, that must be exceeded before the tenure points are granted. */
    private static final long TENURE_FISCAL_YEARS_FOR_POINTS = 1;

    private Membership() {
    }

    /** The owner's membership points, from 0 upwards. */
    public static int pointsOf(Owner owner) {
        return pointsOf(owner, owner.getHouseholdMemberCount());
    }

    /**
     * The owner's membership points as if its household had {@code householdMemberCount} members —
     * every factor except the household award is read from the owner as stored; the household award
     * (+2 for {@value #HOUSEHOLD_SIZE_FOR_POINTS} or more members) is granted from the supplied count.
     * Used to score existing household members in the household as it currently stands rather than as
     * it stood when each was created.
     */
    public static int pointsOf(Owner owner, Integer householdMemberCount) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (householdMemberCount != null && householdMemberCount >= HOUSEHOLD_SIZE_FOR_POINTS) {
            points += 2;
        }
        if (FiscalYears.elapsedSinceRegistration(owner) > TENURE_FISCAL_YEARS_FOR_POINTS) {
            points += 3;
        }
        return points;
    }

    /** The owner's numeric membership level, mapped from {@link #pointsOf(Owner)}: 1 for 0-1
     * points, 2 for 2-3, 3 for 4-5, 4 for 6 or more. */
    public static int levelOf(Owner owner) {
        return levelForPoints(pointsOf(owner));
    }

    /** The numeric membership level the owner would hold in a household of the given size (see
     * {@link #pointsOf(Owner, Integer)}). */
    public static int levelOf(Owner owner, Integer householdMemberCount) {
        return levelForPoints(pointsOf(owner, householdMemberCount));
    }

    /** Map membership points to a level: 1 for 0-1, 2 for 2-3, 3 for 4-5, 4 for 6 or more. */
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
}
