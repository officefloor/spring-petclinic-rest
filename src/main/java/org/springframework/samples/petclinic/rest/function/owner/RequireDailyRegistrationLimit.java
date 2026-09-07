package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyRegistrationLimitException;

/**
 * Step of {@code POST /api/owners} that rejects a create when 100 or more owners have already been
 * registered on the same adjusted business day (by {@link Owner#getRegistrationDate()}), throwing
 * {@link DailyRegistrationLimitException} (handled as 429 Too Many Requests). The business day is
 * the effective registration date (supplied on the request or defaulted to today) rolled forward
 * off a weekend to the next Monday (see {@link BusinessDay}), matching the date
 * {@link BuildOwner}/{@link SaveOwner} will store for the new owner.
 */
public class RequireDailyRegistrationLimit {

    /** Maximum number of owners permitted to register in a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyRegistrationLimitException {
        LocalDate businessDay = BusinessDay.effectiveRegistrationDate(request.getRegistrationDate());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyRegistrationLimitException(
                    "The daily limit of " + DAILY_LIMIT + " owner registrations has been reached");
        }
    }
}
