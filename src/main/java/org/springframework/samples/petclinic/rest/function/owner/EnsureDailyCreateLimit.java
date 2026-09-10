package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.BusinessDays;

/**
 * Rejects a create-owner request once the maximum number of owners has already been
 * created for its business day, responding 429. The day is the request's effective
 * registration date (supplied, else the server date) rolled forward onto a business
 * day: at most {@value #DAILY_LIMIT} owners may carry that adjusted date. Runs after
 * {@link ValidateOwnerFields} and before {@link BuildOwner}, so the owner being created
 * is not yet counted.
 */
public class EnsureDailyCreateLimit {

    /** Maximum number of owners that may be registered in a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyCreateLimitException {
        LocalDate effective = request.getRegistrationDate() != null ? request.getRegistrationDate() : LocalDate.now();
        LocalDate businessDay = BusinessDays.adjust(effective);
        long count = ownerRepository.findAll().stream()
                .filter(o -> businessDay.equals(o.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyCreateLimitException(DAILY_LIMIT);
        }
    }
}
