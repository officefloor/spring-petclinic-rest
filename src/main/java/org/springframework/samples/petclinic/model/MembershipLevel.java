package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The owner's membership level, from 1 to 4. It starts at {@link #BASE}, gains a level when
 * an email address is present and another when the owner has no namesakes
 * ({@code namesakeCount} is 0); those factors are capped at {@link #MAX}. The top level,
 * {@link #TENURE_MAX}, is reserved for tenure: it is reached only once the owner has been
 * registered for more than {@link #TENURE_DAYS} days. A newly created owner has zero tenure,
 * so a new owner never exceeds {@link #MAX}.
 */
public final class MembershipLevel {

    /** The starting level, before any bonuses. */
    public static final int BASE = 1;

    /** The highest level attainable before tenure; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    /** The highest level attainable, reached only with tenure over {@link #TENURE_DAYS}. */
    public static final int TENURE_MAX = 4;

    /** The tenure, in days, that must be exceeded to reach {@link #TENURE_MAX}. */
    public static final int TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /**
     * The membership level for the given owner as of today (see
     * {@link #of(Owner, LocalDate)}).
     */
    public static int of(Owner owner) {
        return of(owner, LocalDate.now());
    }

    /**
     * The membership level for the given owner as of {@code asOf}: {@link #BASE}, plus one
     * when an email address is present and one when the owner has no namesakes (together
     * capped at {@link #MAX}), plus one more — reaching {@link #TENURE_MAX} — once the
     * owner's tenure exceeds {@link #TENURE_DAYS} days.
     */
    public static int of(Owner owner, LocalDate asOf) {
        int level = BASE;
        if (owner.hasEmail()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        level = Math.min(level, MAX);
        if (hasTenure(owner, asOf)) {
            level++;
        }
        return level;
    }

    /** Whether the owner's tenure at {@code asOf} exceeds {@link #TENURE_DAYS} days. */
    private static boolean hasTenure(Owner owner, LocalDate asOf) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate != null
            && ChronoUnit.DAYS.between(registrationDate, asOf) > TENURE_DAYS;
    }
}
