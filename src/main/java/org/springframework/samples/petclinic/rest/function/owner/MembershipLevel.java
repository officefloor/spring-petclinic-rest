package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's {@code membershipLevel}. The pre-tenure factors — an email on file and no
 * namesake — lift the level toward 3. Level 4 additionally requires tenure of more than 365 days
 * since {@code registrationDate}, so a newly created owner (zero tenure) never exceeds level 3.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        if (tenureDays(owner) > 365) {
            level++;
        }
        return Math.min(4, level);
    }

    private static long tenureDays(Owner owner) {
        LocalDate registration = owner.getRegistrationDate();
        return registration == null ? 0 : ChronoUnit.DAYS.between(registration, LocalDate.now());
    }
}
