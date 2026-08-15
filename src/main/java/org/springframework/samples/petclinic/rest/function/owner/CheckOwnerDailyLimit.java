package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once a business day's creation quota is exhausted: if 100 or more
 * owners already carry the {@code registrationDate} this owner would receive, no further owner may
 * be created for that day. The counted day is the effective registration date (supplied on the
 * request or defaulted to the server date) rolled forward onto a business day, matching how
 * {@link BuildOwner} stamps the new owner via {@link BusinessDay#rollForward(LocalDate)}, so the
 * owners this rule counts are exactly those sharing the resulting day. Runs after
 * {@link ValidateNewOwner} has published the request, and before {@link BuildOwner}, so a full day
 * is a 429 rather than a persisted record.
 */
public class CheckOwnerDailyLimit {

    /** Maximum number of owners that may be created in a single day. */
    private static final long DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        // Count owners against the same adjusted business day BuildOwner will stamp on this owner,
        // so a weekend create is bucketed with the Monday it rolls forward to.
        LocalDate businessDay = BusinessDay.rollForward(effective);
        long createdThatDay = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        if (createdThatDay >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(createdThatDay);
        }
    }
}
