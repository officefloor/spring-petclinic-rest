package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's membership level (1 to 4). The base level starts at 1 and gains a point for a
 * usable email address and another when the owner has no namesakes. Level 4 is reserved for tenured
 * members: it additionally requires a tenure of more than 365 days measured from the registration
 * date. Because a newly created owner has zero tenure, a new owner never exceeds level 3.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** Returns the membership level for an owner with the given {@code email}, {@code namesakeCount}
     *  and {@code registrationDate}, as at {@code today}. */
    public static int of(String email, Integer namesakeCount, LocalDate registrationDate, LocalDate today) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        // Level 4 requires tenure over 365 days, so a brand-new owner (zero tenure) never exceeds 3.
        if (registrationDate != null && today != null
            && ChronoUnit.DAYS.between(registrationDate, today) > 365) {
            level++;
        }
        return Math.min(4, level);
    }
}
