package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request once {@link #DAILY_LIMIT} or more owners have already been
 * registered on the current date, so the request maps to a 429. Owners are counted by their
 * {@code registrationDate}, which defaults to today on creation. Runs among the other create
 * guards, before {@link BuildOwner} maps the request to an entity.
 */
public class EnsureDailyLimit {

    /** Maximum number of owners allowed to be registered on a single date. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(today, DAILY_LIMIT);
        }
    }
}
