package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric membership level, assigned on create.
 *
 * <p>The level starts at 1, gains 1 when an email is present and gains 1 when the owner's
 * {@code namesakeCount} is 0 (the name was unique on create). These pre-tenure factors are held at or
 * below {@link #PRE_TENURE_MAX} (3) by a guard, so email presence and a unique name carry an owner no
 * higher than level 3 on their own. Level 4 is reserved for tenure and is awarded only once an owner
 * has been registered for more than {@link #TENURE_DAYS_FOR_MAX} (365) days; a newly created owner has
 * zero tenure, so it never exceeds level 3.
 *
 * <p>This replaces the former string membership tier and is the single source of truth for the
 * computation shared by {@code OwnerMapper} (the {@code membershipLevel} response field) and the
 * create audit line.
 */
public final class MembershipLevel {

    /** The highest level this rule awards. */
    public static final int MAX = 4;

    /** Guard ceiling for the pre-tenure factors (email, unique name) before the tenure bump. */
    public static final int PRE_TENURE_MAX = 3;

    /** An owner must have been registered for more than this many days to be eligible for level 4. */
    public static final long TENURE_DAYS_FOR_MAX = 365;

    private MembershipLevel() {
    }

    /**
     * Compute the membership level for an owner.
     *
     * @param owner the owner; its {@code email}, {@code namesakeCount} and tenure drive the result
     * @return the level, between 1 and {@link #MAX} inclusive
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.hasEmail()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, PRE_TENURE_MAX); // guard: pre-tenure factors cannot reach level 4
        if (tenureDays(owner) > TENURE_DAYS_FOR_MAX) {
            level++;
        }
        return Math.min(level, MAX);
    }

    /**
     * Days elapsed since the owner's registration date, or 0 when it is unknown (e.g. a brand-new
     * owner being created). Never negative for a future registration date.
     */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return Math.max(days, 0);
    }
}
