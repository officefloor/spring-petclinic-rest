package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Step of {@code POST /api/owners} that rejects a create with 429 when {@value #DAILY_LIMIT} or
 * more owners have already been registered today (by {@code registrationDate}). New owners default
 * to today's date, so once the day is full no further owner may be created until tomorrow.
 */
public class RequireDailyOwnerLimit {

    /** Maximum number of owners that may be registered on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitExceededException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(today);
        }
    }
}
