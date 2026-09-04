package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.TooManyOwnersTodayException;

/**
 * Rejects a create request once the maximum number of owners (100) have already been created today,
 * counted by registration date, so no more than 100 owners are registered per day. Runs before
 * {@link BuildOwner}; a day already at its limit is a 429 via {@link TooManyOwnersTodayException}.
 */
public class EnsureDailyOwnerLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws TooManyOwnersTodayException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new TooManyOwnersTodayException();
        }
    }
}
