package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitException;

/**
 * Rejects a new owner once {@value #DAILY_LIMIT} or more owners have already been
 * created for the same business day, counted by {@code registrationDate} equal to this
 * owner's adjusted registration date. Runs after {@link DefaultOwnerRegistrationDate}
 * (so the incoming owner already has its business-day registration date) and before
 * {@link SaveOwner}: when the day is full it throws a checked
 * {@link OwnerDailyLimitException}, which the escalation handler turns into a 429 Too
 * Many Requests.
 */
public class RejectOwnerDailyLimit {

    /** Maximum number of owners that may be created on a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerDailyLimitException {
        LocalDate businessDay = owner.getRegistrationDate();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never count the owner itself
            }
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new OwnerDailyLimitException(
                    "The maximum number of owners for today has already been reached");
        }
    }
}
