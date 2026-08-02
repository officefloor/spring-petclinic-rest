package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects creating an owner once {@link #DAILY_LIMIT} owners have already been registered for the
 * new owner's registration date. Runs before {@link SaveOwner} so an over-cap owner never reaches
 * the data store; the thrown escalation maps to 400 Bad Request.
 */
public class CheckDailyOwnerLimit {

    private static final int DAILY_LIMIT = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return; // no registration date to enforce the daily cap against
        }
        long sameDay = ownerRepository.findAll().stream()
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        if (sameDay >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(
                    "The maximum of " + DAILY_LIMIT + " owners for " + registrationDate
                            + " has already been reached");
        }
    }
}
