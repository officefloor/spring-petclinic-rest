package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level. The level starts at 1, gains 1 when the
 * owner has an email address and gains 1 when the owner has no namesakes
 * ({@code namesakeCount == 0}); these creation-time factors cap at {@link #NEW_OWNER_MAX}.
 * A further level is gained once the owner's tenure — measured from its registration date
 * — exceeds {@link #TENURE_DAYS} days, taking it to {@link #MAX}. Because a newly created
 * owner has zero tenure, a new owner never exceeds {@link #NEW_OWNER_MAX}.
 */
public final class MembershipLevel {

    /** Level assigned to every owner before any bonuses. */
    public static final int BASE = 1;

    /** Highest level attainable from the creation-time factors alone; level 4 also
     *  requires tenure. */
    public static final int NEW_OWNER_MAX = 3;

    /** Highest level attainable. */
    public static final int MAX = 4;

    /** Days of tenure an owner must exceed to reach {@link #MAX}. */
    public static final int TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /** The membership level for the given owner as of today. */
    public static int of(Owner owner) {
        return of(owner, LocalDate.now());
    }

    /** The membership level for the given owner, with tenure measured as of {@code asOf}. */
    public static int of(Owner owner, LocalDate asOf) {
        int level = BASE;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, NEW_OWNER_MAX);
        if (hasTenure(owner.getRegistrationDate(), asOf)) {
            level++;
        }
        return level;
    }

    /** Whether the tenure from {@code registrationDate} to {@code asOf} exceeds
     *  {@link #TENURE_DAYS}; false when either date is absent. */
    private static boolean hasTenure(LocalDate registrationDate, LocalDate asOf) {
        if (registrationDate == null || asOf == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, asOf) > TENURE_DAYS;
    }
}
