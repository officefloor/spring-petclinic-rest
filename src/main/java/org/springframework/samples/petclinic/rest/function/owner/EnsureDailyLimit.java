package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request once {@link #DAILY_LIMIT} or more owners have already been
 * registered on the request's effective business day, so the request maps to a 429. Owners
 * are counted by their stored {@code registrationDate}, which is rolled forward onto a
 * business day on creation; this guard rolls the request's supplied-or-defaulted date the
 * same way (see {@link RegistrationDates}) so both sides count the same day. Runs among the
 * other create guards, before {@link BuildOwner} maps the request to an entity.
 */
public class EnsureDailyLimit {

    /** Maximum number of owners allowed to be registered on a single business day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate day = RegistrationDates.effective(request.getRegistrationDate());
        if (DailyRegistrations.countOn(ownerRepository, day) >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(day, DAILY_LIMIT);
        }
    }
}
