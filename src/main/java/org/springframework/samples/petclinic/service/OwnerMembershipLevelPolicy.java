package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Business rule: an owner earns {@code membershipPoints} starting at 0 &mdash; plus 2 when an email
 * is present, plus 1 when {@code namesakeCount} is 0, plus 2 for a household of 3 or more, plus 3 for
 * tenure over one fiscal year &mdash; and those points map to a numeric {@code membershipLevel}: 1 (0-1),
 * 2 (2-3), 3 (4-5), 4 (6 or more). Kept as a small, self-contained unit so the rule can be applied
 * from the read and audit flows without adding complexity to the mapper, controller, or service.
 */
@Component
public class OwnerMembershipLevelPolicy {

    private static final long TENURE_FISCAL_YEARS = 1;

    private static final int LARGE_HOUSEHOLD = 3;

    private static ClinicService clinicService;

    OwnerMembershipLevelPolicy(ClinicService clinicService) {
        OwnerMembershipLevelPolicy.clinicService = clinicService;
    }

    /**
     * Derive the {@code membershipPoints} for the given owner.
     *
     * @param owner the owner whose membership points to derive
     * @return the points, 0 or more
     */
    public static int membershipPoints(Owner owner) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            points += 2;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (householdSize(owner) >= LARGE_HOUSEHOLD) {
            points += 2;
        }
        if (exceedsTenure(owner.getRegistrationDate())) {
            points += 3;
        }
        return points;
    }

    /**
     * Map the owner's {@code membershipPoints} to a {@code membershipLevel} from 1 to 4.
     *
     * @param owner the owner whose membership level to derive
     * @return the level, from 1 to 4
     */
    public static int membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
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

    /** Count the owners sharing this owner's {@code householdId}, including the owner itself. */
    private static int householdSize(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /** True when tenure (elapsed fiscal years since registration) is more than the level-4 threshold. */
    private static boolean exceedsTenure(LocalDate registrationDate) {
        return registrationDate != null
            && OwnerFiscalYearPolicy.fiscalYearOf(LocalDate.now())
                - OwnerFiscalYearPolicy.fiscalYearOf(registrationDate) > TENURE_FISCAL_YEARS;
    }
}
