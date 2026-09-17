package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The membership points system. An owner's score starts at 0 and gains points for the
 * factors below; the score then maps to a membership level. The single place these rules
 * live, keeping {@link AssignMembershipLevel} to orchestration.
 *
 * <ul>
 *   <li>+2 when an email is on file,</li>
 *   <li>+1 when the owner has no namesakes ({@code namesakeCount} is 0),</li>
 *   <li>+2 for a household of {@value #LARGE_HOUSEHOLD_SIZE} or more,</li>
 *   <li>+3 for tenure over {@value #LONG_TENURE_DAYS} days.</li>
 * </ul>
 *
 * The resulting points map to a level: 1 for 0-1, 2 for 2-3, 3 for 4-5, 4 for 6 or more.
 */
final class MembershipPoints {

    private static final int EMAIL_POINTS = 2;
    private static final int NO_NAMESAKES_POINTS = 1;
    private static final int LARGE_HOUSEHOLD_POINTS = 2;
    private static final int LONG_TENURE_POINTS = 3;

    /** Smallest household size that earns the household points. */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;
    /** Tenure, in days, that must be exceeded to earn the tenure points. */
    private static final long LONG_TENURE_DAYS = 365;

    private MembershipPoints() {
    }

    /** The owner's membership score from its factors. */
    static int score(String email, Integer namesakeCount, long householdSize, long tenureDays) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += EMAIL_POINTS;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += NO_NAMESAKES_POINTS;
        }
        if (householdSize >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureDays > LONG_TENURE_DAYS) {
            points += LONG_TENURE_POINTS;
        }
        return points;
    }

    /** The membership level a score maps to: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6+). */
    static int level(int points) {
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
