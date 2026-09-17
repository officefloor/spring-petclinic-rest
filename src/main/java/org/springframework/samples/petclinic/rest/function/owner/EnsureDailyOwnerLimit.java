package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Create-owner step: rejects the request with a 429 when {@link #DAILY_LIMIT} or more owners
 * have already been registered today (by {@link Owner#getRegistrationDate()}). Reads only the
 * existing owners, so it can run before the new owner is built.
 */
public class EnsureDailyOwnerLimit {

    /** The maximum number of owners that may be created on a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int createdToday = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (today.equals(owner.getRegistrationDate())) {
                createdToday++;
            }
        }
        if (createdToday >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DAILY_LIMIT);
        }
    }
}
