package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a create request once {@value #DAILY_LIMIT} or more owners already carry the new owner's
 * (adjusted business-day) registrationDate, so exceeding the daily cap is a 429 via
 * {@link DailyLimitExceededException}.
 */
public class EnsureDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate day = owner.getRegistrationDate();
        long registeredToday = ownerRepository.findAll().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        if (registeredToday >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(registeredToday);
        }
    }
}
