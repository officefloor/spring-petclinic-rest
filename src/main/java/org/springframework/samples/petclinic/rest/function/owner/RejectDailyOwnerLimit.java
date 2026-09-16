package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once {@link #DAILY_LIMIT} or more owners have already been
 * registered on the new owner's business day, responding 429. Counts against the effective
 * registration date resolved by {@link ResolveOwnerRegistrationDate} — the supplied-or-defaulted
 * date already rolled onto a business day — so the count and the new owner share one day.
 */
public class RejectDailyOwnerLimit {

    /** Maximum number of owners that may be registered in a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DAILY_LIMIT);
        }
    }
}
