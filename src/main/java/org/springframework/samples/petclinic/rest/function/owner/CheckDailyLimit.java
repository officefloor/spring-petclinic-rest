package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a new owner once {@link #DAILY_LIMIT} or more owners have already been created today (by
 * {@code registrationDate}), so the request is answered with a 429 Too Many Requests rather than
 * exceeding the daily cap. Runs among the create-time checks, before {@link BuildOwner}.
 */
public class CheckDailyLimit {

    /** Maximum number of owners that may be created in a single day. */
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
