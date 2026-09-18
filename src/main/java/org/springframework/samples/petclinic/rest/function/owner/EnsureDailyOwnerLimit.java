package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once the current day has already reached its owner quota, before
 * {@link BuildOwner} runs. A day is full once {@value #DAILY_LIMIT} or more owners carry today's
 * registration date, so the next one would exceed the cap. Rejects with 429 otherwise.
 */
public class EnsureDailyOwnerLimit {

    /** Maximum owners that may be registered on a single day; the next create is rejected. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.countByRegistrationDate(today);
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(today, count);
        }
    }
}
