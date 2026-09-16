package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * Rejects a create-owner request once the maximum number of owners ({@value #DAILY_LIMIT})
 * have already been registered on the new owner's (adjusted, business-day)
 * {@link Owner#getRegistrationDate() registrationDate}, responding 429 via
 * {@link DailyRegistrationLimitException}. Runs after the registration date has been
 * resolved so the count buckets by the same business day the new owner would be stored under.
 */
public class RequireDailyRegistrationCapacity {

    /** Maximum owners that may be registered on a single business day; the request is rejected once reached. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyRegistrationLimitException {
        LocalDate registrationDate = owner.getRegistrationDate();
        long registeredThatDay = ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(registrationDate::equals)
            .count();
        if (registeredThatDay >= DAILY_LIMIT) {
            throw new DailyRegistrationLimitException(DAILY_LIMIT);
        }
    }
}
