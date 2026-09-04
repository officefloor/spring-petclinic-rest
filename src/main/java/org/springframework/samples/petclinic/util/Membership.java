package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's membership standing as a points total and the level it maps to.
 * Points start at 0 and accrue: +2 for a present email, +1 when the owner has no
 * namesakes, +2 for a household of three or more (see {@link GoldTier}), and +3 for
 * tenure spanning more than one elapsed fiscal year (see {@link FiscalYear}). Levels
 * map from points: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6+).
 */
public final class Membership {

    private Membership() {
    }

    /** Total membership points for {@code owner}. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (Boolean.TRUE.equals(owner.getHouseholdGold())) {
            points += 2;
        }
        if (FiscalYear.of(LocalDate.now()) - FiscalYear.of(owner.getRegistrationDate()) > 1) {
            points += 3;
        }
        return points;
    }

    /** Membership level (1-4) for a points total. */
    public static int level(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }
}
