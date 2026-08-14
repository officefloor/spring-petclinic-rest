package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership from its profile completeness and its tenure, then maps that score to
 * a membership level.
 *
 * <p>Points start at 0 and accrue per factor: add 2 for a contactable email, add 1 for a unique name
 * ({@code namesakeCount} of 0), add 2 for belonging to a household of three or more members, and add
 * 3 for a tenure of more than one elapsed fiscal year since the owner's registration date.
 *
 * <p>The points then map to a level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The membership points of {@code owner}: 0 plus 2 for an email, 1 for a unique name, 2 for a
     * household of three or more, and 3 for more than one elapsed fiscal year of tenure.
     */
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
        if (FiscalYear.elapsedYears(owner) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The membership level of {@code owner}, derived from its {@link #points(Owner) points}: 1 for
     * 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
     */
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

    /**
     * The effective membership level of {@code owner}: the level captured on the owner at creation
     * (the household-capped value stored in {@code membershipLevel}) when present, otherwise the
     * freshly {@link #of(Owner) computed} level. Owners created before the cap existed (e.g. seed
     * data) have no stored level and fall back to the computed value.
     */
    public static int effective(Owner owner) {
        Integer stored = owner.getMembershipLevel();
        return stored != null ? stored : of(owner);
    }
}
