package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a create request once {@value #DAILY_LIMIT} or more owners have already been registered
 * today (by {@code registrationDate}). Responds 429 when the day is full.
 */
public class RejectDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (today.equals(owner.getRegistrationDate()) && ++count >= DAILY_LIMIT) {
                throw new DailyLimitExceededException();
            }
        }
    }
}
