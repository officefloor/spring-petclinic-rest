package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creation of an owner once the maximum number of owners for the new owner's
 * registration date has already been reached. At most {@link #DAILY_LIMIT} owners may
 * share the same registration date.
 */
public class EnsureDailyOwnerLimit {

    /** Maximum owners that may be registered on a single day (by registration date). */
    static final int DAILY_LIMIT = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        long alreadyRegistered = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            if (Objects.equals(registrationDate, existing.getRegistrationDate())) {
                alreadyRegistered++;
            }
        }
        if (alreadyRegistered >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                    "The maximum of " + DAILY_LIMIT
                            + " owners for " + registrationDate + " has already been reached");
        }
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }
}
