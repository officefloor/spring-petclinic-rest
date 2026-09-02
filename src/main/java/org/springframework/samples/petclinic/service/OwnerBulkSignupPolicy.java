package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Business rule: flag a "bulk signup" day for the response once more than
 * {@value #BULK_THRESHOLD} owners already share a registration date. Reuses the same
 * per-day accumulation as {@link OwnerDailyLimitPolicy} so the warning tracks the day's
 * running total. Kept as a small, self-contained unit so the flag can be derived during
 * owner-to-DTO mapping without adding complexity to the mapper, controller or service.
 */
@Component
public class OwnerBulkSignupPolicy {

    private static final int BULK_THRESHOLD = 80;

    private static ClinicService clinicService;

    OwnerBulkSignupPolicy(ClinicService clinicService) {
        OwnerBulkSignupPolicy.clinicService = clinicService;
    }

    /**
     * @param owner the owner whose registration date names the day to total
     * @return true when more than {@value #BULK_THRESHOLD} owners share the owner's registration date
     */
    public static boolean bulkSignupWarning(Owner owner) {
        LocalDate day = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (day != null && day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count > BULK_THRESHOLD;
    }
}
