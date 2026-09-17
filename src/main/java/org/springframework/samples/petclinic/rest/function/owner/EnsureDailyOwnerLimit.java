package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create request once its business day's registrations have reached capacity: the day is
 * full when {@link #DAILY_LIMIT} or more existing owners already carry the new owner's (adjusted)
 * registration date. Runs after {@link ResolveRegistrationDate}, so a weekend request counts against
 * the Monday it rolled to. A full day is a 429 via {@link DailyOwnerLimitException}.
 */
public class EnsureDailyOwnerLimit {

    /** Maximum number of owners that may be registered on a single business day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate day = owner.getRegistrationDate();
        long registeredThatDay = ownerRepository.findAll().stream()
                .filter(existing -> day.equals(existing.getRegistrationDate()))
                .count();
        if (registeredThatDay >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DAILY_LIMIT);
        }
    }
}
