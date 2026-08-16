package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects creating an owner once {@value #MAX_PER_DAY} or more owners have already been created for
 * the new owner's business day (counted by registrationDate equal to the effective, business-day
 * adjusted registration date resolved by {@link ResolveRegistrationDate}), so the create endpoint
 * responds 429 instead of exceeding the daily sign-up cap. Runs after {@link ResolveRegistrationDate}
 * (which publishes the adjusted date) and before {@link BuildOwner}.
 */
public class CheckDailyLimit {

    /** Maximum owners that may be created per day; the next create once reached is rejected. */
    static final int MAX_PER_DAY = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= MAX_PER_DAY) {
            throw new DailyLimitExceededException(count);
        }
    }
}
