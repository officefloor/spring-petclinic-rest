package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once {@link #DAILY_LIMIT} or more owners have already
 * been registered on the current day (by {@link Owner#getRegistrationDate()}), responding
 * 429. Counted the same way {@link DefaultOwnerRegistrationDate} dates a new owner: the
 * server's current date.
 */
public class RejectDailyOwnerLimit {

    /** Maximum number of owners that may be registered in a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DAILY_LIMIT);
        }
    }
}
