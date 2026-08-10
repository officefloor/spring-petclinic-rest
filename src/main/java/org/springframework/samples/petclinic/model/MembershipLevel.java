package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * Derives an owner's membership points and the numeric membership level bucketed from them.
 *
 * <p>Points start at {@code 0} and accumulate: {@code +2} when an email address is present;
 * {@code +1} when the owner has no namesakes ({@code namesakeCount} is {@code 0}); {@code +2} for a
 * household of {@code 3} or more members; {@code +3} for tenure of more than {@code 1} elapsed fiscal
 * year — the number of fiscal-year boundaries (1 July) between the owner's {@code registrationDate}
 * and today.
 *
 * <p>Points map to a level of {@code 1} ({@code 0}-{@code 1} points), {@code 2} ({@code 2}-{@code 3}),
 * {@code 3} ({@code 4}-{@code 5}) or {@code 4} ({@code 6} or more).
 */
public final class MembershipLevel {

    /** Elapsed fiscal years that must be exceeded to earn the tenure points. */
    private static final long TENURE_FISCAL_YEARS_FOR_BONUS = 1;

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
        if (tenureFiscalYears(owner) > TENURE_FISCAL_YEARS_FOR_BONUS) {
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

    /** Compute the membership level (1..4) for the given owner, capped by the owner's
     *  {@code membershipLevelCap} when one is set. The cap is one above the maximum membership level
     *  among the owner's existing household members at creation (see
     *  {@link org.springframework.samples.petclinic.rest.function.owner.AssignMembershipLevelCap});
     *  when {@code null} the derived level applies unchanged. */
    public static int of(Owner owner) {
        int level = levelForPoints(points(owner));
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /** Elapsed fiscal years (1 July boundaries) between the owner's {@code registrationDate} and
     *  today; {@code 0} when unknown. */
    private static long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return FiscalYear.of(LocalDate.now()) - FiscalYear.of(registrationDate);
    }
}
