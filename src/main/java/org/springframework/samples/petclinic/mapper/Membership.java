package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

import java.time.LocalDate;

/**
 * Derives an owner's loyalty standing from a points system. {@link #pointsOf(Owner)} starts at 0
 * and adds 2 when an email is present, 1 when {@code namesakeCount} is 0, 2 for a household of 3 or
 * more (see {@code householdSize}), and 3 for a tenure of more than one elapsed fiscal year (see
 * {@link FiscalYear}). {@link #levelOf(Owner)} maps
 * those points to a numeric {@code membershipLevel}: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4
 * for 6 or more. Because a newly created owner has zero tenure, a new owner reaches at most 5
 * points and so never exceeds level 3. Kept out of {@link OwnerMapper} so MapStruct does not
 * mistake the helper for an implicit mapping method.
 */
public final class Membership {

    /** Points added when the owner has a non-blank email. */
    private static final int POINTS_EMAIL = 2;

    /** Points added when the owner's name is unique ({@code namesakeCount} is 0). */
    private static final int POINTS_UNIQUE_NAME = 1;

    /** Points added when the owner belongs to a household of this size or larger. */
    private static final int POINTS_LARGE_HOUSEHOLD = 2;

    /** The household size (members, inclusive) at which the household factor applies. */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Points added when the owner's tenure exceeds {@link #TENURE_FISCAL_YEARS_FOR_POINTS}. */
    private static final int POINTS_TENURE = 3;

    /** A tenure of strictly more than this many elapsed fiscal years earns the tenure points. */
    private static final int TENURE_FISCAL_YEARS_FOR_POINTS = 1;

    private Membership() {
    }

    /** The loyalty points for {@code owner}. */
    public static int pointsOf(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += POINTS_EMAIL;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += POINTS_UNIQUE_NAME;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_SIZE) {
            points += POINTS_LARGE_HOUSEHOLD;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && FiscalYear.elapsedSince(registrationDate, LocalDate.now()) > TENURE_FISCAL_YEARS_FOR_POINTS) {
            points += POINTS_TENURE;
        }
        return points;
    }

    /** The numeric membership level (1..4) for {@code owner}, mapped from {@link #pointsOf(Owner)}. */
    public static int levelOf(Owner owner) {
        int points = pointsOf(owner);
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
