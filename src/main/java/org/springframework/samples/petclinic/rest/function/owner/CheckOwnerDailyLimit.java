package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.function.common.BusinessDays;

/**
 * Rejects a create-owner request once 100 or more owners have already been created for the same
 * business day, throwing {@link DailyOwnerLimitException} for a 429. The day counted is the
 * request's effective, business-day-adjusted {@code registrationDate} (the supplied date or, when
 * omitted, the server date, with any weekend rolled forward to Monday) — the same date
 * {@link BuildOwner} stamps on the new owner. Runs before the owner is saved, so the count excludes
 * the owner being created: the 100th owner of the day is accepted, the 101st is rejected.
 */
public class CheckOwnerDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate effective = BusinessDays.rollToBusinessDay(supplied != null ? supplied : LocalDate.now());
        long count = ownerRepository.findAll().stream()
                .filter(existing -> effective.equals(existing.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException();
        }
    }
}
