package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request once 100 or more owners have already been registered on the
 * business day this owner will land on (by {@code registrationDate}). Runs before
 * {@link BuildOwner}, using the same effective registration date it will store — the date
 * supplied on the request, or the server date when omitted — rolled forward to a business day
 * (see {@link BusinessDay}). It counts existing owners whose registration date equals that
 * business day; at the limit the request is rejected 429 via {@link DailyLimitExceededException}.
 */
public class RejectDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyLimitExceededException {
        LocalDate effective = request.getRegistrationDate() != null ? request.getRegistrationDate() : LocalDate.now();
        LocalDate businessDay = BusinessDay.roll(effective);
        long count = ownerRepository.findAll().stream()
                .filter(o -> businessDay.equals(o.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(
                    "The maximum number of owners for today has already been reached");
        }
    }
}
