package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership standing from a points score. Points start at 0 and gain
 * 2 for a present email, 1 when namesakeCount is 0, 2 for a household of 3 or more, and 3
 * for a tenure of more than 365 days measured from the registration date. The score maps to
 * a level of 1 (0-1 points), 2 (2-3), 3 (4-5) or 4 (6 or more).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership points of {@code owner}. */
    public static int points(Owner owner) {
        return (owner.getEmail() != null ? 2 : 0)
            + (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0 ? 1 : 0)
            + (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3 ? 2 : 0)
            + (ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now()) > 365 ? 3 : 0);
    }

    /** The membership level of {@code owner} (1-4), mapped from its {@link #points(Owner)}. */
    public static int of(Owner owner) {
        return Math.min(4, points(owner) / 2 + 1);
    }
}
