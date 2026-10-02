package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a new owner once {@link RegistrationDates#DAILY_LIMIT} or more owners have already been
 * created for its registration day, so the request is answered with a 429 Too Many Requests rather than
 * exceeding the daily cap. The cap is counted per adjusted business day: the request's effective
 * registration date (supplied, or the server date when omitted) is rolled forward over weekends exactly
 * as {@link BuildOwner} will store it, so stored owners already carry the same adjusted dates. Runs
 * among the create-time checks, before {@link BuildOwner}.
 */
public class CheckDailyLimit {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate businessDay = RegistrationDates.effective(request.getRegistrationDate());
        if (RegistrationDates.countOn(businessDay, ownerRepository) >= RegistrationDates.DAILY_LIMIT) {
            throw new DailyLimitExceededException(RegistrationDates.DAILY_LIMIT);
        }
    }
}
