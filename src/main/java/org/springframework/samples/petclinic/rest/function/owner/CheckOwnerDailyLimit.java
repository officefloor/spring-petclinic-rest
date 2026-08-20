package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitException;

/**
 * Rejects a create-owner request with a 429 once {@value #DAILY_LIMIT} or more owners have already
 * been registered today (by {@code registrationDate}). Runs before the owner is saved, so the count
 * reflects existing owners only.
 */
public class CheckOwnerDailyLimit {

    /** No more owners may be created today once this many already carry today's registration date. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerDailyLimitException {
        // Count against the adjusted business day this owner will actually be registered on:
        // the effective date (supplied or defaulted to today) rolled forward off a weekend.
        LocalDate effective = request.getRegistrationDate() != null ? request.getRegistrationDate() : LocalDate.now();
        LocalDate businessDay = BusinessDay.roll(effective);
        long count = ownerRepository.findAll().stream()
                .map(Owner::getRegistrationDate)
                .filter(businessDay::equals)
                .count();
        if (count >= DAILY_LIMIT) {
            throw new OwnerDailyLimitException((int) count);
        }
    }
}
