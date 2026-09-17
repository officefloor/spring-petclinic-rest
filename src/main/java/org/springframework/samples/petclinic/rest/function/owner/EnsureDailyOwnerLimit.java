package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create request once the day's registrations have reached capacity: the day is full when
 * {@link #DAILY_LIMIT} or more existing owners already carry today's registration date. A full day is a
 * 429 via {@link DailyOwnerLimitException}.
 */
public class EnsureDailyOwnerLimit {

    /** Maximum number of owners that may be registered on a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long registeredToday = ownerRepository.findAll().stream()
                .filter(owner -> today.equals(owner.getRegistrationDate()))
                .count();
        if (registeredToday >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DAILY_LIMIT);
        }
    }
}
