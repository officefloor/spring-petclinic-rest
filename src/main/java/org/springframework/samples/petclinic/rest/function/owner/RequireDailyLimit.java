package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.util.BusinessDays;

/**
 * Rejects a create request once {@link #DAILY_LIMIT} or more owners have already been
 * registered today (by {@code registrationDate}). Runs before the owner is built, so the
 * count reflects the owners that already exist. Reported as a 429 by
 * {@link org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededExceptionHandler}.
 */
public class RequireDailyLimit {

    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        // Count against the effective registration date for this request, rolled forward
        // to a business day exactly as BuildOwner will store it, so the limit is per
        // adjusted business day (a weekend request counts toward the following Monday).
        LocalDate supplied = request.getRegistrationDate();
        LocalDate effective = BusinessDays.toBusinessDay(supplied == null ? LocalDate.now() : supplied);
        long count = ownerRepository.findAll().stream()
            .filter(existing -> effective.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(DAILY_LIMIT);
        }
    }
}
