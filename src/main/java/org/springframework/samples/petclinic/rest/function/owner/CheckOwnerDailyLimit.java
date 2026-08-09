package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitException;

/**
 * Rejects a create-owner request once the daily sign-up limit is reached — 100 or more owners have
 * already been created for the request's business day, counted by
 * {@link Owner#getRegistrationDate() registrationDate} equal to the effective registration date.
 * That effective date is the one supplied in the request (or the server's current date when
 * omitted), rolled forward to the next business day (see
 * {@link RegistrationDates#toBusinessDay(LocalDate)}) exactly as {@link BuildOwner} stores it, so
 * the limit counts owners per adjusted business day. Throws {@link OwnerDailyLimitException}
 * (handled as 429) when the limit is met or exceeded.
 */
public class CheckOwnerDailyLimit {

    /** The maximum number of owners permitted to be created on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerDailyLimitException {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate businessDay = RegistrationDates.toBusinessDay(supplied != null ? supplied : LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new OwnerDailyLimitException(businessDay, count);
        }
    }
}
