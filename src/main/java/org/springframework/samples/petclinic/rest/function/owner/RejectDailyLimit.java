package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitException;

/**
 * Rejects a create request once 100 or more owners have already been registered
 * on the current business day (by registrationDate), so the endpoint responds 429.
 */
public class RejectDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitException {
        LocalDate today = BusinessDay.onOrAfter(LocalDate.now());
        long count = ownerRepository.findAll().stream()
                .filter(o -> today.equals(o.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitException();
        }
    }
}
