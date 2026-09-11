package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYears;

/**
 * Derives an owner's membership {@code points} and numeric {@code level}.
 *
 * <p>Points start at 0 and accumulate: add 2 when an email is present, add 1 when
 * {@code namesakeCount} is 0, add 2 for a household of 3 or more members, and add 3
 * for tenure over one fiscal year.
 *
 * <p>Points map to the level (1-4): 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for
 * 6 or more.
 */
public final class MembershipLevels {

    /** Elapsed fiscal years above which an owner earns the tenure points. */
    private static final long TENURE_FISCAL_YEARS_FOR_BONUS = 1;

    /** Household size at or above which an owner earns the household points. */
    private static final int HOUSEHOLD_SIZE_FOR_BONUS = 3;

    private MembershipLevels() {
    }

    /** The membership points for the given owner. */
    public static int pointsForOwner(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_SIZE_FOR_BONUS) {
            points += 2;
        }
        if (tenureInFiscalYears(owner) > TENURE_FISCAL_YEARS_FOR_BONUS) {
            points += 3;
        }
        return points;
    }

    /**
     * The membership level (1-4) for the given owner, derived from its points and then
     * capped: when a {@code membershipLevelCap} is set (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.AssignMembershipLevelCap}),
     * the level cannot exceed it. A {@code null} cap means no ceiling applies.
     */
    public static int forOwner(Owner owner) {
        int level = rawLevelForOwner(owner);
        Integer cap = owner.getMembershipLevelCap();
        return cap == null ? level : Math.min(level, cap);
    }

    /** The uncapped membership level (1-4) for the given owner, derived from its points. */
    private static int rawLevelForOwner(Owner owner) {
        int points = pointsForOwner(owner);
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
     * Elapsed fiscal years since registration (the number of 1-July fiscal-year
     * boundaries crossed between the business-day-adjusted registration date and today),
     * or 0 when the registration date is unknown.
     */
    private static long tenureInFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return FiscalYears.fiscalYear(LocalDate.now()) - FiscalYears.fiscalYear(registrationDate);
    }
}
