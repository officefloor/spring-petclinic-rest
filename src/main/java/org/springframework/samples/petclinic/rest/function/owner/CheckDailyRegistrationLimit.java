package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * Rejects creating an owner once {@value #DAILY_LIMIT} owners have already been
 * registered today (by registration date), by throwing
 * {@link DailyRegistrationLimitException}, which is handled as a 400 Bad Request.
 */
public class CheckDailyRegistrationLimit {

    static final int DAILY_LIMIT = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyRegistrationLimitException {
        LocalDate today = LocalDate.now();
        long registeredToday = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                registeredToday++;
            }
        }
        if (registeredToday >= DAILY_LIMIT) {
            throw new DailyRegistrationLimitException(
                    "Cannot register more than " + DAILY_LIMIT + " owners in a single day");
        }
    }
}
