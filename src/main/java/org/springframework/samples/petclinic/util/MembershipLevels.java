package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

import java.time.LocalDate;

/**
 * Scores an owner's membership and maps that score to a numeric level. Points start at zero and
 * accrue for each qualifying factor: {@link #EMAIL_POINTS} for a present email,
 * {@link #NO_NAMESAKE_POINTS} for having no namesakes ({@code namesakeCount} is 0),
 * {@link #HOUSEHOLD_POINTS} for a household of {@link #HOUSEHOLD_SIZE} or more, and
 * {@link #TENURE_POINTS} for tenure of at least {@link #TENURE_FISCAL_YEARS} elapsed fiscal
 * year(s) since registration (see {@link FiscalYears}). The total is mapped to a level of 1 to 4
 * by {@link #levelFor(int)}. A newly created owner has zero tenure, so the tenure points are out
 * of reach on creation.
 */
public final class MembershipLevels {

    /** Points added when an email is present. */
    public static final int EMAIL_POINTS = 2;

    /** Points added when the owner has no namesakes ({@code namesakeCount} is 0). */
    public static final int NO_NAMESAKE_POINTS = 1;

    /** Points added for a household of {@link #HOUSEHOLD_SIZE} or more. */
    public static final int HOUSEHOLD_POINTS = 2;

    /** Household size, in members, that must be reached for {@link #HOUSEHOLD_POINTS}. */
    public static final int HOUSEHOLD_SIZE = 3;

    /** Points added for tenure of at least {@link #TENURE_FISCAL_YEARS} elapsed fiscal year(s). */
    public static final int TENURE_POINTS = 3;

    /** Elapsed fiscal years since registration required to earn {@link #TENURE_POINTS}. */
    public static final int TENURE_FISCAL_YEARS = 1;

    private MembershipLevels() {
    }

    /** Return the membership points for {@code owner} as at {@code asOf}. */
    public static int pointsOf(Owner owner, LocalDate asOf) {
        int points = 0;
        if (owner.hasEmail()) {
            points += EMAIL_POINTS;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += NO_NAMESAKE_POINTS;
        }
        if (hasHousehold(owner)) {
            points += HOUSEHOLD_POINTS;
        }
        if (hasTenure(owner, asOf)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * The owner's effective membership level as at {@code asOf}: the points-derived level (see
     * {@link #levelFor(int)}) reduced to the owner's {@link Owner#getMembershipLevelCap() cap} when
     * one applies. A null cap means the level is uncapped.
     */
    public static int levelOf(Owner owner, LocalDate asOf) {
        int level = levelFor(pointsOf(owner, asOf));
        Integer cap = owner.getMembershipLevelCap();
        return cap == null ? level : Math.min(level, cap);
    }

    /** Map membership points to a level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    public static int levelFor(int points) {
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

    /** Whether the owner's household has {@link #HOUSEHOLD_SIZE} or more members. */
    private static boolean hasHousehold(Owner owner) {
        Integer size = owner.getHouseholdSize();
        return size != null && size >= HOUSEHOLD_SIZE;
    }

    /**
     * Whether at least {@link #TENURE_FISCAL_YEARS} fiscal year(s) have elapsed between the owner's
     * registration date and {@code asOf}.
     */
    private static boolean hasTenure(Owner owner, LocalDate asOf) {
        LocalDate registration = owner.getRegistrationDate();
        if (registration == null || asOf == null) {
            return false;
        }
        return FiscalYears.elapsedBetween(registration, asOf) >= TENURE_FISCAL_YEARS;
    }
}
