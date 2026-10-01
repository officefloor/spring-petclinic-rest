package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * Derives an owner's membership standing, assigned on create.
 *
 * <p>A points total is accumulated from four factors, starting at 0:
 * <ul>
 *   <li>+{@link #POINTS_EMAIL} (2) when an email is present;</li>
 *   <li>+{@link #POINTS_UNIQUE_NAME} (1) when the owner's {@code namesakeCount} is 0 (the name was
 *       unique on create);</li>
 *   <li>+{@link #POINTS_HOUSEHOLD} (2) for a household of {@link #HOUSEHOLD_SIZE_FOR_POINTS} (3) or
 *       more members;</li>
 *   <li>+{@link #POINTS_TENURE} (3) when the owner's tenure exceeds
 *       {@link #TENURE_FISCAL_YEARS_FOR_POINTS} (1) elapsed fiscal year.</li>
 * </ul>
 *
 * <p>The points total maps to a numeric level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or
 * more.
 *
 * <p>This is the single source of truth for the computation shared by {@code OwnerMapper} (the
 * {@code membershipPoints} and {@code membershipLevel} response fields) and the create audit line.
 */
public final class MembershipLevel {

    /** The highest level this rule awards. */
    public static final int MAX = 4;

    /** Points added when the owner has an email. */
    public static final int POINTS_EMAIL = 2;

    /** Points added when the owner's name was unique on create ({@code namesakeCount} is 0). */
    public static final int POINTS_UNIQUE_NAME = 1;

    /** Points added for a household of {@link #HOUSEHOLD_SIZE_FOR_POINTS} or more members. */
    public static final int POINTS_HOUSEHOLD = 2;

    /** Points added when the owner's tenure exceeds {@link #TENURE_FISCAL_YEARS_FOR_POINTS}. */
    public static final int POINTS_TENURE = 3;

    /** A household needs at least this many members to earn {@link #POINTS_HOUSEHOLD}. */
    public static final int HOUSEHOLD_SIZE_FOR_POINTS = 3;

    /**
     * An owner's tenure must exceed this many elapsed fiscal years to earn {@link #POINTS_TENURE}.
     * Tenure is counted in fiscal years (which start on 1 July, see {@link FiscalYear}), not in
     * 365-day windows.
     */
    public static final long TENURE_FISCAL_YEARS_FOR_POINTS = 1;

    private MembershipLevel() {
    }

    /**
     * Compute the membership points for an owner.
     *
     * @param owner the owner; its {@code email}, {@code namesakeCount}, {@code householdMemberCount}
     *              and tenure drive the result
     * @return the points total, 0 or greater
     */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.hasEmail()) {
            points += POINTS_EMAIL;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += POINTS_UNIQUE_NAME;
        }
        if (owner.getHouseholdMemberCount() != null
                && owner.getHouseholdMemberCount() >= HOUSEHOLD_SIZE_FOR_POINTS) {
            points += POINTS_HOUSEHOLD;
        }
        if (tenureFiscalYears(owner) > TENURE_FISCAL_YEARS_FOR_POINTS) {
            points += POINTS_TENURE;
        }
        return points;
    }

    /**
     * Compute the membership level for an owner by mapping its {@link #points(Owner) points}.
     *
     * @param owner the owner
     * @return the level, between 1 and {@link #MAX} inclusive
     */
    public static int of(Owner owner) {
        return levelForPoints(points(owner));
    }

    /**
     * Map a points total to a membership level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, 4 for 6 or
     * more.
     *
     * @param points the points total
     * @return the level, between 1 and {@link #MAX} inclusive
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
     * Fiscal years elapsed since the owner's registration date - the count of 1 July boundaries
     * crossed between registration and today - or 0 when the registration date is unknown (e.g. a
     * brand-new owner being created). Never negative for a future registration date.
     */
    private static long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long years = FiscalYear.startYearOf(LocalDate.now()) - FiscalYear.startYearOf(registrationDate);
        return Math.max(years, 0);
    }
}
