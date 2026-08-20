package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership points and the numeric level (1 to 4) mapped from them.
 *
 * <p>Points start at 0 and accumulate: +2 when an email is present, +1 when {@code namesakeCount}
 * is 0, +2 for a household of 3 or more members, and +3 for tenure of more than one elapsed fiscal
 * year (starting 1 July) between the owner's registration date and today.
 *
 * <p>Points map to a level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more. Because a
 * newly created owner has zero tenure, the tenure points are unavailable, so a new owner tops out at
 * 5 points and therefore never exceeds level 3.
 */
public final class MembershipLevels {

    /** The tenure points require strictly more than this many elapsed fiscal years of tenure. */
    private static final int TENURE_FISCAL_YEARS_FOR_TENURE_POINTS = 1;

    private MembershipLevels() {
    }

    /** Computes the owner's membership points. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        if (hasTenurePoints(owner.getRegistrationDate())) {
            points += 3;
        }
        return points;
    }

    /**
     * Computes the owner's membership level from the owner's points, then applies the household
     * level ceiling: when a {@code membershipLevelCap} is set (one above the maximum level among
     * the owner's household members at the time the owner was created), the level is capped to it.
     * A null cap means no ceiling applies.
     */
    public static int of(Owner owner) {
        int level = levelForPoints(points(owner));
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /** Maps points to a level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
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

    private static boolean hasTenurePoints(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return FiscalYear.elapsed(registrationDate, LocalDate.now()) > TENURE_FISCAL_YEARS_FOR_TENURE_POINTS;
    }
}
