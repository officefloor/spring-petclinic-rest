package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a create request once the maximum number of owners has already been registered
 * on the server's current date (by {@link Owner#getRegistrationDate()}). Runs before
 * {@link BuildOwner}, so an over-limit day is a 429 via {@link DailyLimitExceededException}
 * before any owner is built or saved.
 */
public class EnsureDailyLimit {

    /** Maximum owners that may be registered on a single day; the next create is rejected. */
    static final long MAX_OWNERS_PER_DAY = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        long todayCount = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                todayCount++;
            }
        }
        if (todayCount >= MAX_OWNERS_PER_DAY) {
            throw new DailyLimitExceededException(MAX_OWNERS_PER_DAY);
        }
    }
}
