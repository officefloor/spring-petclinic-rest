package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * The owner's membership scoring. Points start at 0 and accrue independently:
 * {@link #EMAIL_POINTS} when an email address is present, {@link #NO_NAMESAKE_POINTS} when
 * the owner has no namesakes ({@code namesakeCount} is 0), {@link #HOUSEHOLD_POINTS} for a
 * household of {@link #HOUSEHOLD_MIN} or more, and {@link #TENURE_POINTS} once the owner's
 * tenure reaches at least {@link #TENURE_FISCAL_YEARS} elapsed {@link FiscalYear fiscal
 * year}. Those points map to a level from 1 to 4 (see {@link #level(int)}). A newly created
 * owner has zero elapsed fiscal years, so tenure points never apply at creation.
 */
public final class MembershipLevel {

    /** Points awarded when an email address is present. */
    public static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes ({@code namesakeCount} is 0). */
    public static final int NO_NAMESAKE_POINTS = 1;

    /** Points awarded for a household of {@link #HOUSEHOLD_MIN} or more. */
    public static final int HOUSEHOLD_POINTS = 2;

    /** The household size at or above which {@link #HOUSEHOLD_POINTS} is awarded. */
    public static final int HOUSEHOLD_MIN = 3;

    /** Points awarded once the owner's tenure reaches {@link #TENURE_FISCAL_YEARS} elapsed
     *  fiscal years. */
    public static final int TENURE_POINTS = 3;

    /** The number of elapsed fiscal years the tenure must reach to earn {@link #TENURE_POINTS}. */
    public static final int TENURE_FISCAL_YEARS = 1;

    private MembershipLevel() {
    }

    /**
     * The membership points for the given owner as of today (see
     * {@link #points(Owner, LocalDate)}).
     */
    public static int points(Owner owner) {
        return points(owner, LocalDate.now());
    }

    /**
     * The membership points for the given owner as of {@code asOf}: 0 plus
     * {@link #EMAIL_POINTS} when an email address is present, {@link #NO_NAMESAKE_POINTS}
     * when the owner has no namesakes, {@link #HOUSEHOLD_POINTS} for a household of
     * {@link #HOUSEHOLD_MIN} or more, and {@link #TENURE_POINTS} once the owner's tenure
     * reaches at least {@link #TENURE_FISCAL_YEARS} elapsed fiscal years.
     */
    public static int points(Owner owner, LocalDate asOf) {
        int points = 0;
        if (owner.hasEmail()) {
            points += EMAIL_POINTS;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += NO_NAMESAKE_POINTS;
        }
        Integer householdMemberCount = owner.getHouseholdMemberCount();
        if (householdMemberCount != null && householdMemberCount >= HOUSEHOLD_MIN) {
            points += HOUSEHOLD_POINTS;
        }
        if (hasTenure(owner, asOf)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * The membership level for the given {@code points}: 1 for 0-1 points, 2 for 2-3, 3 for
     * 4-5, and 4 for 6 or more.
     */
    public static int level(int points) {
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

    /**
     * The membership level for the given owner as of today (see
     * {@link #of(Owner, LocalDate)}).
     */
    public static int of(Owner owner) {
        return of(owner, LocalDate.now());
    }

    /**
     * The membership level for the given owner as of {@code asOf}: {@link #points(Owner,
     * LocalDate)} mapped to a level by {@link #level(int)}, then held at or below the
     * owner's {@link Owner#getMembershipLevelCap() household ceiling} when one applies.
     */
    public static int of(Owner owner, LocalDate asOf) {
        int level = level(points(owner, asOf));
        Integer cap = owner.getMembershipLevelCap();
        return cap == null ? level : Math.min(level, cap);
    }

    /**
     * Whether the owner's tenure at {@code asOf} reaches at least
     * {@link #TENURE_FISCAL_YEARS} elapsed fiscal years since registration.
     */
    private static boolean hasTenure(Owner owner, LocalDate asOf) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate != null
            && FiscalYear.elapsed(registrationDate, asOf) >= TENURE_FISCAL_YEARS;
    }
}
