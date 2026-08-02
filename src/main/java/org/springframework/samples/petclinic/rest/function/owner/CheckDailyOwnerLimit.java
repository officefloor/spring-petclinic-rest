package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects creating an owner once {@value #DAILY_LIMIT} owners have already been
 * registered on the new owner's registration day — by throwing
 * {@link DailyOwnerLimitException} (400).
 */
public class CheckDailyOwnerLimit {

    static final int DAILY_LIMIT = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate day = owner.getRegistrationDate();
        if (day == null) {
            day = LocalDate.now();
        }
        long registeredToday = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                registeredToday++;
            }
        }
        if (registeredToday >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(
                    "No more than " + DAILY_LIMIT + " owners may be registered per day");
        }
    }
}
