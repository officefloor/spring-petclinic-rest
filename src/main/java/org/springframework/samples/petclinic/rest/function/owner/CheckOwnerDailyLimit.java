package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once the day's creation quota is exhausted: if 100 or more owners
 * already carry today's {@code registrationDate}, no further owner may be created today. Matches how
 * {@link BuildOwner} stamps a new owner with {@link LocalDate#now()}, so today's creations are
 * exactly the owners this rule counts. Runs after {@link ValidateNewOwner} has published the
 * request, and before {@link BuildOwner}, so a full day is a 429 rather than a persisted record.
 */
public class CheckOwnerDailyLimit {

    /** Maximum number of owners that may be created in a single day. */
    private static final long DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (createdToday >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(createdToday);
        }
    }
}
