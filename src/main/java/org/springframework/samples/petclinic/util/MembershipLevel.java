package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership level, a number from 1 to 4. It starts at 1, gains 1 when an email
 * address is present, gains 1 when the owner has a unique name (namesakeCount is zero), and gains 1
 * when the owner's {@link Tenure tenure} exceeds {@link #TENURE_DAYS} days, capped at 4. Because a
 * newly created owner has zero tenure, a new owner never exceeds level 3 — reaching level 4 always
 * requires tenure of more than a year.
 */
public final class MembershipLevel {

    /** The lowest membership level every owner starts from. */
    public static final int BASE = 1;

    /** The highest level this rule awards. */
    public static final int CAP = 4;

    /** Tenure in days beyond which the top level is awarded. */
    public static final int TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /** The membership level for {@code owner}, or {@code null} when the owner is {@code null}. */
    public static Integer of(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = BASE;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (Tenure.days(owner) > TENURE_DAYS) {
            level++;
        }
        return Math.min(level, CAP);
    }
}
