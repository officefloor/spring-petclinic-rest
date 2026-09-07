package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership points and the numeric membership level mapped from them.
 * Points start at 0 and gain: 2 when an email is present, 1 when {@code namesakeCount} is 0,
 * 2 for a household of 3 or more members, and 3 for tenure of more than one elapsed fiscal
 * year (measured from {@code registrationDate}). Points map to a level (1 to 4): 1 for 0-1
 * points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
 */
public final class MembershipLevel {

    /** Points awarded when an email is present. */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes. */
    private static final int NO_NAMESAKE_POINTS = 1;

    /** Points awarded for a household of {@link #HOUSEHOLD_THRESHOLD} or more members. */
    private static final int HOUSEHOLD_POINTS = 2;

    /** Household size at or above which household points are awarded. */
    private static final int HOUSEHOLD_THRESHOLD = 3;

    /** Points awarded when tenure exceeds {@link #TENURE_FISCAL_YEARS} elapsed fiscal years. */
    private static final int TENURE_POINTS = 3;

    /** Elapsed fiscal years beyond which an owner earns tenure points. */
    private static final long TENURE_FISCAL_YEARS = 1;

    private MembershipLevel() {
    }

    /** Membership points derived from the owner's email, namesake count, household size and tenure. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += NO_NAMESAKE_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_THRESHOLD) {
            points += HOUSEHOLD_POINTS;
        }
        if (elapsedFiscalYears(owner) > TENURE_FISCAL_YEARS) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /** Membership level (1 to 4) mapped from the owner's {@link #points(Owner) membership points}. */
    public static int of(Owner owner) {
        int points = points(owner);
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

    /** Fiscal years elapsed since the owner's registrationDate; 0 when the date is absent or in the future. */
    private static long elapsedFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long years = FiscalYear.of(LocalDate.now()) - FiscalYear.of(registrationDate);
        return Math.max(years, 0);
    }
}
