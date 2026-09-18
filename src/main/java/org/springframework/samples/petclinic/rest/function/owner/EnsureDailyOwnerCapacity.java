package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Runs in {@code POST /api/owners} before any entity is built or persisted. Rejects the
 * request when the resolved business day (the adjusted registrationDate from
 * {@link ResolveOwnerRegistrationDate}) has already reached {@link #DAILY_LIMIT} owner
 * registrations, throwing {@link DailyOwnerLimitException} for a 429. Reads the count in the
 * same transaction as the writes so the per-day cap is enforced consistently.
 */
public class EnsureDailyOwnerCapacity {

    /** Maximum number of owners that may be registered in a single business day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {

        long dayCount = ownerRepository.findAll().stream()
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        if (dayCount >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(registrationDate);
        }
    }
}
