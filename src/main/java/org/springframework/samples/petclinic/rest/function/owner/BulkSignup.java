package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Bulk-signup rule: an owner carries a warning once more than {@value #WARN_THRESHOLD} owners share
 * its registration day, matching the accumulation the daily-limit rule counts.
 */
public final class BulkSignup {

    private static final int WARN_THRESHOLD = 80;

    private BulkSignup() {
    }

    public static boolean warned(Owner owner, OwnerRepository ownerRepository) {
        LocalDate day = owner.getRegistrationDate();
        long registeredThatDay = ownerRepository.findAll().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        return registeredThatDay > WARN_THRESHOLD;
    }
}
