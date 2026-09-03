package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create once {@value #DAILY_LIMIT} or more owners already share the new
 * owner's (business-day adjusted) registration date.
 */
public class EnsureDailyOwnerLimitNotExceeded {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate registrationDate = owner.getRegistrationDate();
        long count = ownerRepository.findAll().stream()
                .filter(other -> registrationDate.equals(other.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException("The maximum number of owners for today has been reached");
        }
    }
}
