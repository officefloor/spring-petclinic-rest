package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once {@value #DAILY_LIMIT} or more owners have already been
 * registered on the new owner's registrationDate, with a 429 Too Many Requests. That date has
 * already been rolled onto a business day by {@link BuildOwner}, so the limit is per adjusted
 * business day. Runs after {@link BuildOwner} and before the owner is saved, so the count reflects
 * only pre-existing owners.
 */
public class CheckOwnerDailyLimit {

    /** Maximum number of owners that may be created in a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        // Count against the new owner's (already business-day-adjusted) registration date, not the
        // raw server date, so the limit applies per adjusted business day.
        LocalDate registrationDate = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // the owner being created is not counted against the limit
            }
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
