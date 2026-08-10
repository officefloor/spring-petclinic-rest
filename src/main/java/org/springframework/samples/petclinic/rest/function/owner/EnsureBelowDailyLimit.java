package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Runs before {@link BuildOwner}. Rejects the request when 100 or more owners have already been
 * created today (compared by {@code registrationDate} against {@link LocalDate#now()}), by throwing
 * {@link DailyOwnerLimitException} (handled as 429 Too Many Requests).
 */
public class EnsureBelowDailyLimit {

    /** The maximum number of owners permitted to be created in a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
