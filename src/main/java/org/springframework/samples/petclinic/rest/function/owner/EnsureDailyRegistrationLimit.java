package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * Rejects a create once 100 or more owners have already been registered today, counted
 * by {@link Owner#getRegistrationDate()} against the server's current date. Runs before
 * the owner is saved, so the count excludes the owner being created; a full day is
 * rejected 429 via {@link DailyRegistrationLimitException}.
 */
public class EnsureDailyRegistrationLimit {

    /** Maximum number of owners that may be registered on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyRegistrationLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyRegistrationLimitException(today, count);
        }
    }
}
