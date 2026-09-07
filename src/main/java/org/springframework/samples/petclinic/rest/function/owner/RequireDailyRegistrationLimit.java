package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * Step of {@code POST /api/owners} that rejects a create when 100 or more owners have already been
 * registered today (by {@link Owner#getRegistrationDate()}), throwing
 * {@link DailyRegistrationLimitException} (handled as 429 Too Many Requests). Runs before
 * {@link BuildOwner}/{@link SaveOwner} persist the new owner.
 */
public class RequireDailyRegistrationLimit {

    /** Maximum number of owners permitted to register in a single day. */
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
            throw new DailyRegistrationLimitException(
                    "The daily limit of " + DAILY_LIMIT + " owner registrations has been reached");
        }
    }
}
