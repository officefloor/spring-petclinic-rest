package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's loyalty standing, exposed as two fields the create audit line and the owner
 * response both hang off: {@code membershipPoints} (the raw score) and {@code membershipLevel} (the
 * grade the points bucket into).
 *
 * <p>Points start at 0 and accumulate from the owner's factors: {@link #EMAIL_POINTS} for a present
 * email address, {@link #NO_NAMESAKE_POINTS} when {@code namesakeCount} is 0,
 * {@link #LARGE_HOUSEHOLD_POINTS} for a household of {@link #LARGE_HOUSEHOLD_SIZE} or more, and
 * {@link #TENURE_POINTS} for more than {@link #TENURE_DAYS_FOR_POINTS} days of tenure since
 * {@code registrationDate}. A freshly created owner registers as of today and so has zero tenure.
 *
 * <p>Points map to a level of 1 (0-1 points), 2 (2-3), 3 (4-5) or 4 (6 or more).
 */
public final class Memberships {

    /** Points awarded for a present (non-blank) email address. */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes ({@code namesakeCount == 0}). */
    private static final int NO_NAMESAKE_POINTS = 1;

    /** Points awarded for belonging to a household of {@link #LARGE_HOUSEHOLD_SIZE} or more. */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Household size (members inclusive) at which {@link #LARGE_HOUSEHOLD_POINTS} is awarded. */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Points awarded for more than {@link #TENURE_DAYS_FOR_POINTS} days of tenure. */
    private static final int TENURE_POINTS = 3;

    private static final int TENURE_DAYS_FOR_POINTS = 365;

    private Memberships() {
    }

    /**
     * The owner's {@code membershipPoints}: the raw loyalty score built from a present email, no
     * namesakes, a large household and tenure. {@code householdSize} is the number of owners sharing
     * this owner's household (members inclusive).
     */
    public static int membershipPoints(Owner owner, long householdSize) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += EMAIL_POINTS;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += NO_NAMESAKE_POINTS;
        }
        if (householdSize >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (hasTenureForPoints(owner)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * The owner's {@code membershipLevel}: the grade {@code membershipPoints} buckets into — 1 for
     * 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
     */
    public static int membershipLevel(int points) {
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

    private static boolean hasTenureForPoints(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long tenureDays = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return tenureDays > TENURE_DAYS_FOR_POINTS;
    }
}
