package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Step of {@code POST /api/owners}: rejects the request 429 once 100 or more owners already carry
 * today's registration date. New owners default to today's date (see {@link ApplyRegistrationDate}),
 * so this caps the number of owners that can be registered in a single day. Runs before
 * {@link BuildOwner} so no owner is persisted on rejection.
 */
public class RequireDailyCapacity {

    /** Maximum number of owners permitted to register on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException();
        }
    }
}
