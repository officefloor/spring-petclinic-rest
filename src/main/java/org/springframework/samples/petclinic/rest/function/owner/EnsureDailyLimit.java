package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects creating an owner when 100 or more owners have already been created today,
 * matched by {@code registrationDate} equal to the current date. The count is taken over
 * the existing owners (the new owner is not yet saved). At 100 or more the request is a
 * 429 Too Many Requests (see {@link DailyLimitExceededException}).
 */
public class EnsureDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        long createdToday = ownerRepository.findAll().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        if (createdToday >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(createdToday);
        }
    }
}
