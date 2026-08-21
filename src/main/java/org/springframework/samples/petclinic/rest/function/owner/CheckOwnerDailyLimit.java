package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once 100 or more owners have already been registered on the same
 * adjusted business day (owners whose {@code registrationDate} equals this request's effective,
 * business-day-adjusted registration date — supplied or defaulted to the server date, with a weekend
 * rolled forward to Monday). Runs before the owner is saved, so the count excludes the owner being
 * created. Throws {@link DailyOwnerLimitException} (handled as 429) when that day's limit is reached.
 */
public class CheckOwnerDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate day = BusinessDay.effective(request.getRegistrationDate());
        int count = (int) ownerRepository.findAll().stream()
            .filter(o -> day.equals(o.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(day, count);
        }
    }
}
