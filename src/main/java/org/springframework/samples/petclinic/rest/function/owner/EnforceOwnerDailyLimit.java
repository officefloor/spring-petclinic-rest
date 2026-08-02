package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitException;

/**
 * Rejects creating an owner once the maximum number of owners for a single day has
 * already been reached - with a 400 via {@link OwnerDailyLimitException}. The day is
 * the owner's registration date, and the count is the number of existing owners
 * sharing that same registration date. As {@link BuildOwner} defaults a missing
 * registration date to today, a normal create is limited to at most
 * {@value #MAX_OWNERS_PER_DAY} owners registered today.
 */
public class EnforceOwnerDailyLimit {

    static final int MAX_OWNERS_PER_DAY = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws OwnerDailyLimitException {
        LocalDate registrationDate = owner.getRegistrationDate();
        long registeredThatDay = ownerRepository.findAll().stream()
                .filter(existing -> Objects.equals(existing.getRegistrationDate(), registrationDate))
                .count();
        if (registeredThatDay >= MAX_OWNERS_PER_DAY) {
            throw new OwnerDailyLimitException(
                    "The maximum of " + MAX_OWNERS_PER_DAY + " owners for a single day has already been reached");
        }
    }
}
