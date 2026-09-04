package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership level rule: a numeric level from 1 to 4. It starts at 1, gains 1 when the owner has an
 * email address, gains 1 when the owner has no namesakes, and gains 1 for tenure of more than 365
 * days. It is capped at 4. A newly created owner has zero tenure, so a new owner never exceeds 3.
 */
public final class MembershipLevel {

    private static final int CAP = 4;

    private static final long TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (hasTenure(owner)) {
            level++;
        }
        return Math.min(level, CAP);
    }

    private static boolean hasTenure(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && ChronoUnit.DAYS.between(registered, LocalDate.now()) > TENURE_DAYS;
    }
}
