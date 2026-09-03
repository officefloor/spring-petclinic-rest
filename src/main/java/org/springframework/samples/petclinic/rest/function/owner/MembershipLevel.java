package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives an owner's membership standing as points, then a level. Points accrue from an
 * email (+2), a zero namesake count (+1), a household of three or more (+2) and tenure of
 * more than one elapsed fiscal year since registration (+3). The points map to a level: 1 (0-1), 2 (2-3),
 * 3 (4-5), 4 (6 or more).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** Membership points for the owner (see class doc). */
    public static int points(Owner owner, OwnerRepository ownerRepository) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (HouseholdTier.isGold(owner, ownerRepository)) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > 1) {
            points += 3;
        }
        return points;
    }

    /** Membership level for a points total: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
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

    private static int tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate == null ? 0 : FiscalYear.elapsed(registrationDate, LocalDate.now());
    }
}
