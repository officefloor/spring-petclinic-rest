package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership scoring for an owner. Points accrue from independent factors — 2 for a supplied email,
 * 1 for a {@code namesakeCount} of 0, 2 for a household of three or more occupants (the owner plus
 * their pets) and 3 for tenure of more than 365 days since {@code registrationDate} — and are banded
 * into a level of 1 through 4.
 */
public final class OwnerMembershipLevel {

    private OwnerMembershipLevel() {
    }

    /** The owner's membership points. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (1 + owner.getPets().size() >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > 365) {
            points += 3;
        }
        return points;
    }

    /** The owner's membership level, 1 through 4, banded from {@link #points(Owner)}. */
    public static int of(Owner owner) {
        int points = points(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? 0 : ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
