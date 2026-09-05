package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create-owner request once {@link #DAILY_LIMIT} or more owners have already been created
 * for the request's adjusted business day, counting existing owners whose {@code registrationDate}
 * equals that day. The day is the effective registration date (supplied in the request or defaulted
 * to the server date) rolled forward off any weekend to the next Monday, matching how
 * {@link BuildOwner} stores it. Runs before the owner is built and saved. At capacity the request is
 * rejected via {@link DailyLimitExceededException}, which the global handler turns into a 429.
 */
public class RejectOverDailyLimit {

    /** Maximum owners that may be created in a single day; the request is rejected once this many exist. */
    static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate effective = request.getRegistrationDate() != null ? request.getRegistrationDate() : LocalDate.now();
        LocalDate day = BusinessDay.adjust(effective);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(DAILY_LIMIT);
        }
    }
}
