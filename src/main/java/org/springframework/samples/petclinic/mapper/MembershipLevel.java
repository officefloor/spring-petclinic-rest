package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership points and numeric membership level from their persisted fields.
 *
 * <p>Points start at 0; add 2 when an email is present; add 1 when {@code namesakeCount} is 0; add
 * 2 for a household of 3 or more; add 3 for tenure over 365 days. Tenure is the number of days from
 * the registration date to today, so a newly created owner has zero tenure and never earns the
 * tenure points: a fresh owner tops out at 5 points.
 *
 * <p>The membership level maps the points: 1 (0-1 points), 2 (2-3), 3 (4-5), 4 (6 or more).
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for an implicit mapping method. Single source of truth so the response DTO and
 * the create audit line agree on the points and level.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership points for {@code owner}. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
                && ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /** The membership level (1 to 4) for {@code owner}, derived from {@link #points(Owner)}. */
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
}
