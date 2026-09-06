package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects creating an owner when 100 or more owners already share this owner's registration date,
 * counted by {@link Owner#getRegistrationDate()}. That date is the adjusted business day, so the
 * cap is enforced per business day. The owner being created is not yet persisted, so it is not part
 * of the count.
 */
public class EnsureDailyOwnerLimit {

    private static final int MAX_OWNERS_PER_DAY = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate registrationDate = owner.getRegistrationDate();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException(
                    MAX_OWNERS_PER_DAY + " or more owners have already been created for "
                            + registrationDate);
        }
    }
}
