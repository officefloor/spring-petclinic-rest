package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a create-owner request once 100 or more owners have already been registered on this
 * request's adjusted business day (by registrationDate), so the escalation handler can respond 429.
 * Runs after {@link DefaultRegistrationDate} has resolved and business-day-adjusted the entity's
 * registrationDate, and within the same write transaction as the insert so the count reflects only
 * owners already persisted (excluding this new, not-yet-saved one), giving a hard cap of 100 owners
 * created per business day.
 */
public class CheckDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate day = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(count);
        }
    }
}
