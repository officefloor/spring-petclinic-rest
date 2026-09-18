package org.springframework.samples.petclinic.model;

/**
 * Membership scoring for an owner. Loyalty factors accrue points from a base of zero &mdash; an
 * email address ({@value #EMAIL_POINTS}), having had no namesakes at creation
 * ({@code namesakeCount} of 0, {@value #NO_NAMESAKE_POINTS}), belonging to a household of
 * {@link #LARGE_HOUSEHOLD_SIZE} or more ({@value #LARGE_HOUSEHOLD_POINTS}) and a tenure of more than
 * {@link #TENURE_DAYS_FOR_TENURE_POINTS} days ({@value #TENURE_POINTS}). The total {@link #points}
 * maps to a numeric {@link #levelFor level} of 1&ndash;4.
 *
 * <p>The email, namesake and household factors are established at creation; only tenure accrues
 * afterwards. A newly created owner has zero tenure, so at creation the points cannot exceed 5 and
 * the level cannot exceed 3.
 */
public final class Membership {

    /** Days of tenure an owner must exceed to earn the tenure points. */
    public static final int TENURE_DAYS_FOR_TENURE_POINTS = 365;

    /** Households with at least this many members earn the household points. */
    public static final int LARGE_HOUSEHOLD_SIZE = 3;

    private static final int EMAIL_POINTS = 2;
    private static final int NO_NAMESAKE_POINTS = 1;
    private static final int LARGE_HOUSEHOLD_POINTS = 2;
    private static final int TENURE_POINTS = 3;

    private Membership() {
    }

    /**
     * The membership points earned by an owner with the given loyalty factors.
     *
     * @param hasEmail      whether the owner has an email address
     * @param namesakeCount how many existing owners shared the owner's name at creation (0 earns
     *                      points); {@code null} earns none
     * @param householdSize how many members the owner's household has
     * @param tenureDays    the owner's tenure in days
     */
    public static int points(boolean hasEmail, Integer namesakeCount, int householdSize, long tenureDays) {
        int points = 0;
        if (hasEmail) {
            points += EMAIL_POINTS;
        }
        if (Integer.valueOf(0).equals(namesakeCount)) {
            points += NO_NAMESAKE_POINTS;
        }
        if (householdSize >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureDays > TENURE_DAYS_FOR_TENURE_POINTS) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * The numeric membership level for a points total: 1 for 0&ndash;1 points, 2 for 2&ndash;3,
     * 3 for 4&ndash;5 and 4 for 6 or more.
     */
    public static int levelFor(int points) {
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
}
