package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request once the maximum number of owners has already been
 * created today, responding 429. "Today" is measured by each owner's
 * {@code registrationDate}: at most {@value #DAILY_LIMIT} owners may carry today's date.
 * Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}, so the owner
 * being created is not yet counted.
 */
public class EnsureDailyCreateLimit {

    /** Maximum number of owners that may be registered in a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyCreateLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
                .filter(o -> today.equals(o.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyCreateLimitException(DAILY_LIMIT);
        }
    }
}
