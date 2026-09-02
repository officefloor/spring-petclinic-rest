package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership. {@link #points(Owner)} sums the owner-only factors — +2 for an
 * email on file, +1 for no namesake, +3 for a tenure of one or more elapsed fiscal years since
 * {@code registrationDate} (the household factor is added later, where the repository is available).
 * {@link #level(int)} buckets a point total into the 1-4 membership level.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (FiscalYear.tenure(owner) >= 1) {
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
