package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Step of {@code POST /api/owners}: rejects the request 429 once 100 or more owners already carry
 * this request's effective registration date. That date is the one supplied in the request, or the
 * server date when none was supplied, rolled forward to the next business day (see
 * {@link ApplyRegistrationDate}), so this caps the number of owners registered on a single business
 * day. Runs before {@link BuildOwner} so no owner is persisted on rejection.
 */
public class RequireDailyCapacity {

    /** Maximum number of owners permitted to register on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate registrationDate = RegistrationDate.effective(request.getRegistrationDate());
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException();
        }
    }
}
