package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric membership level. The level starts at {@link #BASE} and gains a
 * point for each qualifying factor: a present email, having no namesakes ({@code namesakeCount}
 * is 0), and tenure of more than {@link #TENURE_DAYS} days since registration. It is capped at
 * {@link #MAX}. A newly created owner has zero tenure, so the tenure point is out of reach on
 * creation and a new owner never exceeds level 3.
 */
public final class MembershipLevels {

    /** Level every owner starts at. */
    public static final int BASE = 1;

    /** Highest level derivable here; reachable only with the tenure point. */
    public static final int MAX = 4;

    /** Tenure, in days since registration, must exceed this for the tenure point. */
    public static final int TENURE_DAYS = 365;

    private MembershipLevels() {
    }

    /** Return the membership level (1 to 4) for {@code owner} as at {@code asOf}. */
    public static int of(Owner owner, LocalDate asOf) {
        int level = BASE;
        if (owner.hasEmail()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        if (hasTenure(owner, asOf)) {
            level++;
        }
        return Math.min(level, MAX);
    }

    /** Whether the owner's tenure as at {@code asOf} exceeds {@link #TENURE_DAYS} days. */
    private static boolean hasTenure(Owner owner, LocalDate asOf) {
        LocalDate registration = owner.getRegistrationDate();
        if (registration == null || asOf == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registration, asOf) > TENURE_DAYS;
    }
}
