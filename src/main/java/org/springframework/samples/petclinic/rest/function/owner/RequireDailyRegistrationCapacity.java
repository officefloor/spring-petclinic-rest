package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * Rejects a create-owner request once the maximum number of owners ({@value #DAILY_LIMIT})
 * have already been registered on the current date, responding 429 via
 * {@link DailyRegistrationLimitException}. Owners are counted by their
 * {@link Owner#getRegistrationDate() registrationDate} matching today.
 */
public class RequireDailyRegistrationCapacity {

    /** Maximum owners that may be registered on a single date; the request is rejected once reached. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyRegistrationLimitException {
        LocalDate today = LocalDate.now();
        long registeredToday = ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(today::equals)
            .count();
        if (registeredToday >= DAILY_LIMIT) {
            throw new DailyRegistrationLimitException(DAILY_LIMIT);
        }
    }
}
