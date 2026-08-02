package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creating an owner once {@value #DAILY_LIMIT} owners already share the new
 * owner's registration date. Groups by registration date, so the cap resets each day.
 */
public class EnsureDailyOwnerLimit {

    static final int DAILY_LIMIT = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        long existingToday = ownerRepository.findAll().stream()
                .filter(existing -> Objects.equals(registrationDate, existing.getRegistrationDate()))
                .count();
        if (existingToday >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                    "Daily limit of " + DAILY_LIMIT + " owners for " + registrationDate
                            + " has been reached");
        }
    }
}
