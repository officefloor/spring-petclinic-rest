package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared counting over the owners already registered on a given (adjusted, business-day)
 * {@link Owner#getRegistrationDate() registrationDate}. Both the per-day capacity limit and
 * the bulk-signup warning bucket owners by the same day, so they share this count.
 */
final class OwnerRegistrations {

    private OwnerRegistrations() {
    }

    /** The number of stored owners whose registrationDate equals {@code date}. */
    static long countOn(OwnerRepository ownerRepository, LocalDate date) {
        return ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(date::equals)
            .count();
    }
}
