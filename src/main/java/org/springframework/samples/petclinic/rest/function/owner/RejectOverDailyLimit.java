package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request once {@link #DAILY_LIMIT} or more owners have already been created
 * today, counting existing owners whose {@code registrationDate} equals the current date. Runs before
 * the owner is built and saved. At capacity the request is rejected via
 * {@link DailyLimitExceededException}, which the global handler turns into a 429.
 */
public class RejectOverDailyLimit {

    /** Maximum owners that may be created in a single day; the request is rejected once this many exist. */
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
            throw new DailyLimitExceededException(DAILY_LIMIT);
        }
    }
}
