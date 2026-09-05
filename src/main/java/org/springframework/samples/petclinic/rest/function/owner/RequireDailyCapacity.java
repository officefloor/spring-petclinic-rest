package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitException;

/**
 * Rejects a create-owner request once {@value #DAILY_LIMIT} or more owners have already been
 * registered today, before {@link BuildOwner} runs. Owners are counted by registrationDate,
 * which {@link BuildOwner} defaults to today for newly created owners.
 */
public class RequireDailyCapacity {

    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitException {
        LocalDate day = BusinessDay.roll(
            request.getRegistrationDate() == null ? LocalDate.now() : request.getRegistrationDate());
        long count = ownerRepository.findAll().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitException(count);
        }
    }
}
