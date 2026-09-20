package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request once {@link #MAX_OWNERS_PER_DAY} owners have already been registered on
 * the current day. Existing owners are counted by their {@link Owner#getRegistrationDate()
 * registrationDate}, matching the date a new owner defaults to (see {@link DefaultRegistrationDate}),
 * so a full day cannot grow further.
 */
public class EnsureDailyLimit {

    /** Maximum number of owners allowed per day; the next create once reached is rejected. */
    static final int MAX_OWNERS_PER_DAY = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitReachedException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (today.equals(owner.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyLimitReachedException(today);
        }
    }
}
