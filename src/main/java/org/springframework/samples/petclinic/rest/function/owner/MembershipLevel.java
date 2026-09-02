package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level from 1 to 4: starts at 1, adds 1 when an email is
 * present, adds 1 when {@code namesakeCount} is 0, capped at 3 for these factors, then adds 1 for
 * tenure of more than 365 days. A newly created owner has zero tenure, so it never exceeds level 3.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(Owner owner, int namesakeCount) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (namesakeCount == 0) {
            level++;
        }
        level = Math.min(level, 3);
        if (Tenure.days(owner) > 365) {
            level++;
        }
        return level;
    }

    private static final class Tenure {
        static long days(Owner owner) {
            LocalDate registration = owner.getRegistrationDate();
            return registration == null ? 0 : ChronoUnit.DAYS.between(registration, LocalDate.now());
        }
    }
}
