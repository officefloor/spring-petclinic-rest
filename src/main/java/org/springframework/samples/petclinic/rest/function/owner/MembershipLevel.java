package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership level. The pre-tenure factors — an email and a zero
 * namesake count — lift the base level of 1 up to 3, so a brand-new owner never exceeds
 * level 3. Level 4 additionally requires tenure of more than 365 days since registration.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(Owner owner) {
        int base = 1
                + (owner.getEmail() != null && !owner.getEmail().isEmpty() ? 1 : 0)
                + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0);
        int level = Math.min(base, 3);
        return level == 3 && tenureDays(owner) > 365 ? 4 : level;
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? 0 : ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
