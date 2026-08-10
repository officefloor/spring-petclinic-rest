package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once {@value DailyOwnerLimitException#DAILY_LIMIT} or more owners
 * have already been created for the request's business day, counted by {@code registrationDate}. A
 * day at capacity is reported as 429 (see {@link DailyOwnerLimitException}).
 *
 * <p>Runs before {@link BuildOwner}, so a rejected request is never persisted. The bucket is the
 * effective registration date already rolled onto a business day by {@link ResolveRegistrationDate}
 * (supplied in the request or defaulted to today), so the cap counts each business day's
 * registrations — matching the date that {@link BuildOwner} will store.
 */
public class CheckDailyOwnerLimit {

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DailyOwnerLimitException.DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DailyOwnerLimitException.DAILY_LIMIT);
        }
    }
}
