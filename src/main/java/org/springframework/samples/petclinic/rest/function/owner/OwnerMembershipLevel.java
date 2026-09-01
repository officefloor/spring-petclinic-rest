package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership level for an owner. The pre-tenure factors — a base of 1, plus 1 for a supplied
 * email and 1 for a {@code namesakeCount} of 0 — max out at level 3. Level 4 is reached only by
 * tenure of more than 365 days since {@code registrationDate}, so a brand-new owner (zero tenure)
 * never exceeds level 3.
 */
public final class OwnerMembershipLevel {

    private OwnerMembershipLevel() {
    }

    /** The owner's membership level, 1 through 4. */
    public static int of(Owner owner) {
        int base = 1 + (owner.getEmail() != null ? 1 : 0)
                + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0);
        return Math.min(3, base) + (tenureDays(owner) > 365 ? 1 : 0);
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? 0 : ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
