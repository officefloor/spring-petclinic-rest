package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creating an owner once {@value #DAILY_LIMIT} or more owners have already been
 * registered on the same business day (counted by {@link org.springframework.samples.petclinic.model.Owner#getRegistrationDate()}).
 * The count uses the new owner's adjusted registration date — the supplied or defaulted date
 * rolled forward off a weekend (see {@link RegistrationDates}), matching the date {@link BuildOwner}
 * will stamp. A day at capacity is rejected 429 via {@link DailyOwnerLimitExceededException}.
 */
public class CheckOwnerDailyLimit {

    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate businessDay = RegistrationDates.effective(request.getRegistrationDate());
        long registeredThatDay = RegistrationDates.countRegisteredOn(businessDay, ownerRepository);
        if (registeredThatDay >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                    DAILY_LIMIT + " or more owners have already been created on " + businessDay);
        }
    }
}
