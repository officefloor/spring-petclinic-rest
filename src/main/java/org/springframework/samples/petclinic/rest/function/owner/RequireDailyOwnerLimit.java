package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects a create-owner request with a 429 (via {@code DailyOwnerLimitExceededExceptionHandler}) when
 * {@code LIMIT} (100) or more owners have already been created on the new owner's {@code registrationDate}.
 * Runs after {@link DefaultOwnerRegistrationDate}, so the date is populated; the new owner is not
 * persisted yet, so every match found is an existing owner.
 */
public class RequireDailyOwnerLimit {

    private static final int LIMIT = 100;

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        long count = ownerRepository.findAll().stream()
                .map(Owner::getRegistrationDate)
                .filter(registrationDate::equals)
                .count();
        if (count >= LIMIT) {
            throw new DailyOwnerLimitExceededException(LIMIT);
        }
    }
}
