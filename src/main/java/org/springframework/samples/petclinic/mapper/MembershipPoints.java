package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's {@code membershipPoints} and the {@code membershipLevel} derived from
 * them. Points start at 0 and accrue: plus 2 when an email is present, plus 1 when the owner's
 * namesakeCount is 0, plus 2 when the owner belongs to a household of three or more members,
 * and plus 3 when the owner's tenure (elapsed fiscal years since its registration date)
 * exceeds one fiscal year. The level maps the points as 1 (0-1), 2 (2-3), 3 (4-5) and
 * 4 (6 or more).
 *
 * <p>Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit mapping
 * method and apply it to every property.
 */
public final class MembershipPoints {

    private MembershipPoints() {
    }

    /**
     * Compute the owner's membership points.
     *
     * @param owner         the owner to score
     * @param householdSize the number of members in the owner's household (the owner counts as
     *                      one), used for the household-of-three-or-more factor
     * @return the total membership points (never negative)
     */
    public static int points(Owner owner, int householdSize) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (householdSize >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
            && FiscalYear.elapsed(owner.getRegistrationDate(), LocalDate.now()) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Map membership points to a membership level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and
     * 4 for 6 or more.
     *
     * @param points the membership points
     * @return the membership level (1-4)
     */
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
