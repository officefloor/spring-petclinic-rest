package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;

/**
 * The single definition of the membership points → level formula, so every place that needs an owner's
 * level computes it the same way and they cannot drift apart. The household size is not read off the
 * owner here but passed in explicitly ({@code null} for an unknown/single-member household): it depends
 * on other owners and is computed once by {@link OwnerHousehold}, and taking it as an argument keeps
 * this a pure function with no hidden reads or writes.
 *
 * <p>Shared by {@code OwnerMapper} (which exposes {@code membershipPoints} and the capped
 * {@code membershipLevel} on the DTO) and {@link OwnerHousehold} (which reads each other household
 * member's <em>raw</em> level to compute the ceiling). Kept out of the function classes because
 * OfficeFloor requires each function class to expose exactly one public method.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The owner's membership points. Starting from 0: add 2 when an email address is present, add 1
     * when the owner has no namesakes ({@code namesakeCount} is 0), add 2 for a household of 3 or more
     * members, and add 3 when the owner's tenure exceeds one fiscal year (more than one elapsed fiscal
     * year since the {@code registrationDate}; the fiscal year starts on 1 July).
     */
    public static int points(Owner owner, Integer householdSize) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        long tenureFiscalYears = owner.getRegistrationDate() == null ? 0
                : FiscalYear.between(owner.getRegistrationDate(), LocalDate.now());
        if (tenureFiscalYears > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The raw numeric level for a points total: level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for
     * 6 or more. This is the level before any household ceiling is applied.
     */
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

    /** Whether the owner has a usable email address. */
    public static boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }
}
