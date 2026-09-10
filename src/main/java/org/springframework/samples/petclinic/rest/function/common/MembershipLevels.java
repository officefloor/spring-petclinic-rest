package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.util.Collection;

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
 * tenure of more than {@link #TENURE_FISCAL_YEARS_FOR_POINTS} elapsed fiscal years,
 * counted as the difference between the fiscal year of the registration date and the
 * current fiscal year (see {@link FiscalYear}).
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

    /** Points gained for a tenure of more than {@link #TENURE_FISCAL_YEARS_FOR_POINTS}
     *  elapsed fiscal years. */
    public static final int POINTS_TENURE = 3;

    /** Tenure points require strictly more than this many elapsed fiscal years of tenure. */
    public static final int TENURE_FISCAL_YEARS_FOR_POINTS = 1;

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

    /**
     * Compute the membership level for the given owner, capped so it never exceeds one above
     * the highest level among the other members of its household. The {@code householdMembers}
     * are the owner's fellow household members (this owner excluded); when there are none the
     * uncapped level from {@link #of(Owner)} is returned unchanged.
     */
    public static int capped(Owner owner, Collection<Owner> householdMembers) {
        int base = of(owner);
        int ceiling = Integer.MIN_VALUE;
        for (Owner member : householdMembers) {
            ceiling = Math.max(ceiling, of(member));
        }
        if (ceiling == Integer.MIN_VALUE) {
            return base;
        }
        return Math.min(base, ceiling + 1);
    }

    private static boolean hasTenurePoints(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        int elapsedFiscalYears = FiscalYear.of(LocalDate.now()) - FiscalYear.of(registrationDate);
        return elapsedFiscalYears > TENURE_FISCAL_YEARS_FOR_POINTS;
    }
}
