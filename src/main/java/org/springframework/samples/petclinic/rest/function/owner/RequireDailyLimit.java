package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create request once {@value #DAILY_LIMIT} or more owners have already been
 * created today (by registrationDate). At or above the limit rejects 429.
 */
public class RequireDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = BusinessDay.roll(LocalDate.now());
        long count = ownerRepository.findAll().stream()
                .filter(owner -> today.equals(owner.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
