package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitException;

/**
 * Rejects a create-owner request once 100 or more owners have already been created on
 * the request's business day, responding 429 via {@link DailyLimitException}. The day
 * compared is the effective, business-day-adjusted registration date published by
 * {@link ResolveRegistrationDate}, so the count is per adjusted business day.
 *
 * <p>Runs after {@link ResolveRegistrationDate} and before {@link BuildOwner} saves
 * anything, so the count it reads excludes the owner being created — the 101st owner
 * registered on that business day is the first one rejected.
 */
public class EnsureDailyCapacity {

    private static final int DAILY_CAPACITY = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyLimitException {
        long createdThatDay = ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(registrationDate::equals)
            .count();
        if (createdThatDay >= DAILY_CAPACITY) {
            throw new DailyLimitException(registrationDate);
        }
    }
}
