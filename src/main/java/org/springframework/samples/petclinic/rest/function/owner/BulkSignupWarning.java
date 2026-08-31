package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Response flag: true once more than 80 owners share this owner's registration date
 * (i.e. more than 80 were created that day). Recomputed at response time from the same
 * accumulation the daily-registration rule counts.
 */
final class BulkSignupWarning {

    private static final int BULK_THRESHOLD = 80;

    private BulkSignupWarning() {
    }

    static boolean isActive(Owner owner, OwnerRepository ownerRepository) {
        LocalDate day = owner.getRegistrationDate();
        if (day == null) {
            return false;
        }
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count > BULK_THRESHOLD;
    }
}
