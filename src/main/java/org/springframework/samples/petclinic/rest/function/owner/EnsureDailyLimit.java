package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;

/**
 * Rejects a create request once the maximum number of owners has already been registered
 * on the effective registration date resolved by {@link ResolveRegistrationDate} — the
 * adjusted business day this create will land on, not necessarily the server's today. Runs
 * before {@link BuildOwner}, so an over-limit day is a 429 via
 * {@link DailyLimitExceededException} before any owner is built or saved.
 */
public class EnsureDailyLimit {

    /** Maximum owners that may be registered on a single day; the next create is rejected. */
    static final long MAX_OWNERS_PER_DAY = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        long dayCount = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                dayCount++;
            }
        }
        if (dayCount >= MAX_OWNERS_PER_DAY) {
            throw new DailyLimitExceededException(MAX_OWNERS_PER_DAY);
        }
    }
}
