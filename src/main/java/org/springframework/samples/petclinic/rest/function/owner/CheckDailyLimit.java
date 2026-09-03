package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create when 100 or more owners have already been registered today (by
 * registrationDate), before {@link SaveOwner} runs. Handled with 429 by
 * {@code DailyOwnerLimitHandler}.
 */
public class CheckDailyLimit {

    static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
                .filter(other -> today.equals(other.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(today);
        }
    }
}
