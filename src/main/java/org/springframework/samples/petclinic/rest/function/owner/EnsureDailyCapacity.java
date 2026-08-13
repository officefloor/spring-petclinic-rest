package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitException;

/**
 * Rejects a create-owner request once 100 or more owners have already been created
 * today (compared by {@code registrationDate}), responding 429 via
 * {@link DailyLimitException}.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which publishes the request body) and
 * before {@link BuildOwner} saves anything, so the count it reads excludes the owner
 * being created — the 101st owner registered today is the first one rejected.
 */
public class EnsureDailyCapacity {

    private static final int DAILY_CAPACITY = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitException {
        LocalDate today = LocalDate.now();
        long createdToday = ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(today::equals)
            .count();
        if (createdToday >= DAILY_CAPACITY) {
            throw new DailyLimitException(today);
        }
    }
}
