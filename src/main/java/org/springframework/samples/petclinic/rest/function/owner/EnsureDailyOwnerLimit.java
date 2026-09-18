package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once the effective registration day has already reached its
 * owner quota, before {@link BuildOwner} runs. The day counted is the adjusted business day
 * resolved by {@link ResolveRegistrationDate}, so weekend requests count against the Monday they
 * roll forward to. A day is full once {@value #DAILY_LIMIT} or more owners carry that
 * registration date, so the next one would exceed the cap. Rejects with 429 otherwise.
 */
public class EnsureDailyOwnerLimit {

    /** Maximum owners that may be registered on a single day; the next create is rejected. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        long count = ownerRepository.countByRegistrationDate(registrationDate);
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(registrationDate, count);
        }
    }
}
