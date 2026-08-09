package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitException;

/**
 * Rejects a create-owner request once the daily sign-up limit is reached — 100 or more owners have
 * already been created today, counted by {@link Owner#getRegistrationDate() registrationDate} equal
 * to the server's current date. Throws {@link OwnerDailyLimitException} (handled as 429) when the
 * limit is met or exceeded.
 */
public class CheckOwnerDailyLimit {

    /** The maximum number of owners permitted to be created on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws OwnerDailyLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new OwnerDailyLimitException(today, count);
        }
    }
}
