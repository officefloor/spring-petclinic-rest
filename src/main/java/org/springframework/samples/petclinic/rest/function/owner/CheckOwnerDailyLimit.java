package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creating an owner once {@value #DAILY_LIMIT} or more owners have already been
 * registered today (counted by {@link org.springframework.samples.petclinic.model.Owner#getRegistrationDate()}).
 * A day at capacity is rejected 429 via {@link DailyOwnerLimitExceededException}.
 */
public class CheckOwnerDailyLimit {

    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitExceededException {
        LocalDate today = LocalDate.now();
        long registeredToday = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (registeredToday >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                    DAILY_LIMIT + " or more owners have already been created today");
        }
    }
}
