package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership scoring. Points start at 0 and add: +2 when an email is present, +1 when the owner
 * has no namesakes (namesakeCount 0), +2 for a household of 3 or more, +3 for tenure over 365 days.
 * Points map to a level: 1 (0-1 points), 2 (2-3), 3 (4-5), 4 (6 or more).
 */
public final class MembershipPoints {

    private MembershipPoints() {
    }

    public static int of(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (owner.getHouseholdCount() != null && owner.getHouseholdCount() >= 3) {
            points += 2;
        }
        LocalDate registration = owner.getRegistrationDate();
        if (registration != null && FiscalYear.elapsedTo(registration, LocalDate.now()) >= 1) {
            points += 3;
        }
        return points;
    }

    public static int level(int points) {
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
}
