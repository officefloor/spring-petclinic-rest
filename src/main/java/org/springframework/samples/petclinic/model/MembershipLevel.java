package org.springframework.samples.petclinic.model;

/**
 * Membership-level rules for an owner. The level starts at 1 and gains a point for each loyalty
 * factor the owner meets: having an email address, having had no namesakes at creation
 * ({@code namesakeCount} of 0), and a tenure of more than {@link #TENURE_DAYS_FOR_TOP_LEVEL} days.
 *
 * <p>The first two factors are established at creation and so saturate at level 3; the tenure
 * factor is the only route to level 4. A newly created owner has zero tenure, so it can never
 * exceed level 3 &mdash; e.g. an owner with an email and no namesake is level 3, not 4.
 */
public final class MembershipLevel {

    /** Days of tenure an owner must exceed to qualify for the top membership level (4). */
    public static final int TENURE_DAYS_FOR_TOP_LEVEL = 365;

    private MembershipLevel() {
    }

    /**
     * The membership level for an owner with the given loyalty factors. Level 4 requires
     * {@code tenureDays} greater than {@link #TENURE_DAYS_FOR_TOP_LEVEL}; the email and namesake
     * factors alone cannot exceed level 3.
     *
     * @param hasEmail      whether the owner has an email address
     * @param namesakeCount how many existing owners shared the owner's name at creation (0 earns a
     *                      point); {@code null} earns no point
     * @param tenureDays    the owner's tenure in days
     */
    public static int forOwner(boolean hasEmail, Integer namesakeCount, long tenureDays) {
        int level = 1;
        if (hasEmail) {
            level++;
        }
        if (Integer.valueOf(0).equals(namesakeCount)) {
            level++;
        }
        if (tenureDays > TENURE_DAYS_FOR_TOP_LEVEL) {
            level++;
        }
        return level;
    }
}
