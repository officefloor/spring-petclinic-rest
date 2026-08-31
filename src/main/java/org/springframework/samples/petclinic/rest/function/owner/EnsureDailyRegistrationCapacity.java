package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * On create, rejects the owner when 100 or more owners have already been registered today
 * (compared by registrationDate).
 */
public class EnsureDailyRegistrationCapacity {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyRegistrationLimitException {
        LocalDate today = owner.getRegistrationDate();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyRegistrationLimitException("The maximum number of owners for today has been reached");
        }
    }
}
