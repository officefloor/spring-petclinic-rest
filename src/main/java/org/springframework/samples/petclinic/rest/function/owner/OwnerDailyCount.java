package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single home for "how many owners were created on a given (business-day-adjusted)
 * registration date". Both {@link RejectOverDailyLimit} and {@link FlagBulkSignup}
 * key off this count at different thresholds; keeping the predicate here avoids
 * maintaining it in two places.
 */
final class OwnerDailyCount {

    private OwnerDailyCount() {
    }

    /**
     * Counts existing owners whose {@link Owner#getRegistrationDate()} equals
     * {@code registrationDate}. Called before the new owner is saved, so it is not
     * itself counted.
     */
    static long sharingRegistrationDate(OwnerRepository ownerRepository, LocalDate registrationDate) {
        return ownerRepository.findAll().stream()
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
    }
}
