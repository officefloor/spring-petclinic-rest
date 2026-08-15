package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Step of {@code POST /api/owners} that rejects a create with 429 when {@value #DAILY_LIMIT} or
 * more owners have already been registered on the same business day (by {@code registrationDate}).
 * The day counted is the effective, business-day-adjusted registration date computed by
 * {@link DetermineRegistrationDate} — the very date the new owner would be stored with — so once
 * that day is full no further owner may be created for it.
 */
public class RequireDailyOwnerLimit {

    /** Maximum number of owners that may be registered on a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
    }
}
