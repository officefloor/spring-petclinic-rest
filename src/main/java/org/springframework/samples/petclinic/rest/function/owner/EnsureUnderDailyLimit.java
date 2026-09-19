package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request once the maximum number of owners ({@value #DAILY_LIMIT})
 * have already been registered today, responding 429, before {@link BuildOwner} persists
 * one over the limit. Registrations are counted via {@link Registrations}.
 */
public class EnsureUnderDailyLimit {

    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        if (Registrations.countOn(ownerRepository, today) >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(today, DAILY_LIMIT);
        }
    }
}
