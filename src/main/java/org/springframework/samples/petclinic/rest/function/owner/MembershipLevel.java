package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership. Points start at 0 and add 2 for a present email, 1 when the
 * namesake count is 0, 2 for a household of 3 or more and 3 for tenure of at least one
 * elapsed fiscal year. The numeric level maps those points as 1 (0-1), 2 (2-3), 3 (4-5) and
 * 4 (6 or more).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int points(Owner owner, int namesakeCount, int householdSize) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (namesakeCount == 0) {
            points += 1;
        }
        if (householdSize >= 3) {
            points += 2;
        }
        if (hasTenure(owner)) {
            points += 3;
        }
        return points;
    }

    public static int level(int points) {
        return Math.min(points / 2 + 1, 4);
    }

    private static boolean hasTenure(Owner owner) {
        LocalDate registration = owner.getRegistrationDate();
        return registration != null
                && FiscalYear.of(LocalDate.now()) - FiscalYear.of(registration) >= 1;
    }
}
