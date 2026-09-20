package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request once {@link #MAX_OWNERS_PER_DAY} owners have already been registered on
 * the new owner's registration day. Both the new owner (see
 * {@link RollRegistrationDateToBusinessDay}) and existing owners carry a business-day
 * {@link Owner#getRegistrationDate() registrationDate}, so the count is per business day and a full
 * day cannot grow further.
 */
public class EnsureDailyLimit {

    /** Maximum number of owners allowed per day; the next create once reached is rejected. */
    static final int MAX_OWNERS_PER_DAY = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyLimitReachedException {
        LocalDate day = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyLimitReachedException(day);
        }
    }
}
