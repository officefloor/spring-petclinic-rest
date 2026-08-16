package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership points and the membership level those points map to,
 * from the owner's own fields.
 *
 * <p>Points start at 0 and accumulate: {@code +2} when an email address is present,
 * {@code +1} when the owner has no namesakes ({@code namesakeCount} is 0), {@code +2}
 * for a household of 3 or more, and {@code +3} for a tenure (registration date to today)
 * of more than one elapsed fiscal year (the fiscal year starts on 1 July).
 *
 * <p>Points map to a level: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and
 * 4 for 6 or more points.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic mapping method and apply it to unrelated
 * fields; the mapper references it only through explicit expressions.
 */
public final class MembershipPointsDeriver {

    private MembershipPointsDeriver() {
    }

    /**
     * Returns the owner's membership points, derived from the owner's own fields.
     */
    public static int membershipPoints(Owner owner) {
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
            && FiscalYearDeriver.elapsedFiscalYears(owner.getRegistrationDate(), LocalDate.now()) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Returns the membership level for the given membership points: 1 for 0-1 points,
     * 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points.
     */
    public static int membershipLevel(int points) {
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
