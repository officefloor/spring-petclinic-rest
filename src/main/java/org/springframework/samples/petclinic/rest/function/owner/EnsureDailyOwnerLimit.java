package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once the maximum number of owners
 * ({@link DailyOwnerLimitException#LIMIT}) have already been created today, counted by
 * {@link Owner#getRegistrationDate()} equal to the current date. Runs before {@link SaveOwner},
 * so the new owner is not yet counted; a day already holding 100 owners causes the 101st request
 * to fail with 429.
 */
public class EnsureDailyOwnerLimit {

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DailyOwnerLimitException.LIMIT) {
            throw new DailyOwnerLimitException(today, count);
        }
    }
}
