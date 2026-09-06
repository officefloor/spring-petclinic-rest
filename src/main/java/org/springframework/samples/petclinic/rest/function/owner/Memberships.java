package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code membershipLevel}, the single loyalty grade the create audit line and the
 * owner response both hang off.
 *
 * <p>The level is built from the owner's own factors: it starts at 1, gains a level for a present
 * email address and a level for having no namesakes, with those pre-tenure factors capped at
 * {@link #PRE_TENURE_MAX_LEVEL}; the top level ({@link #MAX_MEMBERSHIP_LEVEL}) is reserved for
 * tenure and reached only once the owner has more than {@link #TENURE_DAYS_FOR_TOP_LEVEL} days since
 * {@code registrationDate}. A freshly created owner registers as of today and so has zero tenure.
 */
public final class Memberships {

    /** The top membership level, reachable only with tenure. */
    private static final int MAX_MEMBERSHIP_LEVEL = 4;

    /**
     * The highest level reachable from the pre-tenure factors alone; the top level
     * ({@value #MAX_MEMBERSHIP_LEVEL}) is unlocked only by tenure.
     */
    private static final int PRE_TENURE_MAX_LEVEL = MAX_MEMBERSHIP_LEVEL - 1;

    /**
     * Tenure, in days since {@code registrationDate}, that must be exceeded to reach the
     * top membership level.
     */
    private static final int TENURE_DAYS_FOR_TOP_LEVEL = 365;

    private Memberships() {
    }

    /**
     * Returns the owner's membership level, a number from 1 to
     * {@value #MAX_MEMBERSHIP_LEVEL}: it starts at 1, gains 1 when an email address is
     * present, gains 1 when the owner has no namesakes (namesakeCount is 0), and these
     * pre-tenure factors are capped at {@value #PRE_TENURE_MAX_LEVEL}. Level
     * {@value #MAX_MEMBERSHIP_LEVEL} is reserved for tenure: it is reached only when the
     * owner has more than {@value #TENURE_DAYS_FOR_TOP_LEVEL} days of tenure. A newly
     * created owner registers as of the current day and so has zero tenure, meaning a new
     * owner never exceeds level {@value #PRE_TENURE_MAX_LEVEL}.
     */
    public static int membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        // Level 4 is reserved for tenure: cap the pre-tenure factors, then let more than
        // TENURE_DAYS_FOR_TOP_LEVEL days of tenure unlock the top level.
        level = Math.min(level, PRE_TENURE_MAX_LEVEL);
        if (hasTopLevelTenure(owner)) {
            level++;
        }
        return level;
    }

    /**
     * True when the owner's tenure (days between {@code registrationDate} and today)
     * exceeds {@value #TENURE_DAYS_FOR_TOP_LEVEL}. An owner with no registration date
     * (e.g. legacy seed owners) has no tenure.
     */
    private static boolean hasTopLevelTenure(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        long tenureDays = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return tenureDays > TENURE_DAYS_FOR_TOP_LEVEL;
    }
}
