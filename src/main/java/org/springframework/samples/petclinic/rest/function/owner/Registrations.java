package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared registration-date logic. An owner is registered on the calendar date in their
 * {@code registrationDate} field, so every rule that reasons about a day's registrations
 * (such as the daily creation limit) counts the same set of owners.
 */
final class Registrations {

    private Registrations() {
    }

    /**
     * The number of existing owners registered on the given date.
     */
    static long countOn(OwnerRepository ownerRepository, LocalDate date) {
        return ownerRepository.findAll().stream()
                .filter(existing -> date.equals(existing.getRegistrationDate()))
                .count();
    }
}
