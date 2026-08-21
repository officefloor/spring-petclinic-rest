package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once 100 or more owners have already been registered today
 * (owners whose {@code registrationDate} equals the server's current date). Runs before the owner
 * is saved, so the count excludes the owner being created. Throws {@link DailyOwnerLimitException}
 * (handled as 429) when today's limit has been reached.
 */
public class CheckOwnerDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int count = (int) ownerRepository.findAll().stream()
            .filter(o -> today.equals(o.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(today, count);
        }
    }
}
