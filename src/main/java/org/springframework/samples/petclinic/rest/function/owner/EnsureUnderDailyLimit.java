package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request once the maximum number of owners ({@value #DAILY_LIMIT})
 * have already been registered on this request's business-day registration date, responding
 * 429, before {@link BuildOwner} persists one over the limit. Runs after
 * {@link RollRegistrationDateToBusinessDay} so the count is per adjusted business day.
 * Registrations are counted via {@link Registrations}.
 */
public class EnsureUnderDailyLimit {

    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate date = request.getRegistrationDate();
        if (Registrations.countOn(ownerRepository, date) >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(date, DAILY_LIMIT);
        }
    }
}
