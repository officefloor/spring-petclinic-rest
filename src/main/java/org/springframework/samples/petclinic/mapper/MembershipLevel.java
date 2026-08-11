package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership points and numeric membership level from their persisted fields.
 *
 * <p>Points start at 0; add 2 when an email is present; add 1 when {@code namesakeCount} is 0; add
 * 2 for a household of 3 or more; add 3 for tenure of at least one elapsed fiscal year. Tenure is
 * the number of whole fiscal years (starting 1 July) from the registration date to today, so a
 * newly created owner has zero tenure and never earns the tenure points: a fresh owner tops out at
 * 5 points.
 *
 * <p>The membership level maps the points: 1 (0-1 points), 2 (2-3), 3 (4-5), 4 (6 or more).
 *
 * <p>A membership-level cap may be recorded on the owner (see {@code membershipLevelCap}): a newly
 * created owner joining an existing household cannot exceed one level above the highest membership
 * level already held in that household. When such a cap is present the derived level is clamped to
 * it; with no cap (no existing household member) the points-derived level stands.
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
                && FiscalYear.elapsed(owner.getRegistrationDate(), LocalDate.now()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The membership level (1 to 4) for {@code owner}, derived from {@link #points(Owner)} and
     * clamped to the owner's recorded {@code membershipLevelCap} when one is present.
     */
    public static int of(Owner owner) {
        int level = fromPoints(points(owner));
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /** The uncapped membership level for the given points total. */
    private static int fromPoints(int points) {
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
