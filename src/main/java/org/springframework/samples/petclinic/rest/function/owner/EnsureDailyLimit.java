package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitReachedException;

/**
 * Rejects a create-owner request once 100 or more owners have already been registered on the
 * request's adjusted business day (the effective {@code registrationDate} resolved by
 * {@link ResolveRegistrationDate}), responding 429 via {@link DailyLimitReachedException}. Runs
 * before {@link SaveOwner}, so the count excludes the owner being created.
 */
public class EnsureDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyLimitReachedException {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitReachedException(count);
        }
    }
}
