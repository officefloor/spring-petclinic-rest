package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects creating an owner when 100 or more owners have already been created on the new
 * owner's effective business day. That day is the supplied registration date, or the
 * server date when omitted, rolled forward over a weekend to the following Monday — the
 * same adjustment {@link BuildOwner} stores — so the count is taken per adjusted business
 * day. The count is over the existing owners (the new owner is not yet saved). At 100 or
 * more the request is a 429 Too Many Requests (see {@link DailyLimitExceededException}).
 */
public class EnsureDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate businessDay = BusinessDays.effectiveRegistrationDate(request.getRegistrationDate());
        long createdToday = BusinessDays.countCreatedOn(ownerRepository, businessDay);
        if (createdToday >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(createdToday);
        }
    }
}
