package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Derives an owner's 'membershipPoints' and numeric 'membershipLevel', assigned
 * on create. Kept out of {@link OwnerMapper} so MapStruct does not mistake it for
 * an implicit property mapping method.
 */
public final class MembershipLevelResolver {

    /** Points awarded when an email is present (non-blank). */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner's name was unique on create (namesakeCount is 0). */
    private static final int UNIQUE_NAME_POINTS = 1;

    /** Points awarded for a household of three or more members. */
    private static final int HOUSEHOLD_POINTS = 2;

    /** Points awarded for tenure over the threshold. */
    private static final int TENURE_POINTS = 3;

    /** Elapsed fiscal years that must be exceeded before the tenure points are awarded. */
    private static final int TENURE_THRESHOLD_FISCAL_YEARS = 1;

    /** A household of this many members (or more) earns the household points. */
    private static final int GOLD_HOUSEHOLD_SIZE = 3;

    private MembershipLevelResolver() {
    }

    /**
     * Returns the membership points for an owner. Starts at 0, gains 2 when an
     * email is present (non-blank), gains 1 when the owner's name was unique on
     * create ({@code namesakeCount} is 0), gains 2 when the owner belongs to a
     * household of three or more members and gains 3 for tenure over one elapsed
     * fiscal year.
     *
     * @param email                the owner's email, may be {@code null}
     * @param namesakeCount        the owner's namesake count, may be {@code null}
     * @param householdMemberCount the number of owners in the owner's household, may be {@code null}
     * @param registrationDate     the owner's registration date, may be {@code null}
     * @return the membership points, zero or more
     */
    public static int deriveMembershipPoints(String email, Integer namesakeCount,
            Integer householdMemberCount, LocalDate registrationDate) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += EMAIL_POINTS;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        if (householdMemberCount != null && householdMemberCount >= GOLD_HOUSEHOLD_SIZE) {
            points += HOUSEHOLD_POINTS;
        }
        if (hasTenure(registrationDate)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Returns the membership level for an owner, mapped from its
     * {@link #deriveMembershipPoints membership points}: level 1 (0-1 points),
     * level 2 (2-3 points), level 3 (4-5 points) and level 4 (6 or more points).
     *
     * @param email                the owner's email, may be {@code null}
     * @param namesakeCount        the owner's namesake count, may be {@code null}
     * @param householdMemberCount the number of owners in the owner's household, may be {@code null}
     * @param registrationDate     the owner's registration date, may be {@code null}
     * @return the membership level, between 1 and 4 inclusive
     */
    public static int deriveMembershipLevel(String email, Integer namesakeCount,
            Integer householdMemberCount, LocalDate registrationDate) {
        int points = deriveMembershipPoints(email, namesakeCount, householdMemberCount, registrationDate);
        return levelForPoints(points);
    }

    /**
     * Maps membership points to a membership level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more).
     *
     * @param points the owner's membership points
     * @return the membership level, between 1 and 4 inclusive
     */
    private static int levelForPoints(int points) {
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
     * Reports whether an owner's tenure exceeds the tenure threshold, i.e. more than one whole fiscal
     * year (see {@link FiscalYearResolver}) has elapsed between {@code registrationDate} and today. A
     * {@code null} or future registration date counts as no tenure.
     *
     * @param registrationDate the owner's registration date, may be {@code null}
     * @return {@code true} when more than one fiscal year has elapsed since registration
     */
    private static boolean hasTenure(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return FiscalYearResolver.elapsedFiscalYears(registrationDate, LocalDate.now())
            > TENURE_THRESHOLD_FISCAL_YEARS;
    }
}
