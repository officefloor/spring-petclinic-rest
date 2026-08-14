package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Runs in the create-owner pipeline before {@link BuildOwner}. The daily limit is counted per
 * business day: the incoming request's effective registration date — the value it supplied, or the
 * server's current date when omitted — is adjusted to a business day via {@link BusinessDays}
 * (a weekend rolls forward to the next Monday), matching what {@link DefaultOwnerRegistrationDate}
 * will store. Counts existing owners already registered on that adjusted business day and rejects
 * the request with a 429 once that count has reached the daily limit of 100, so no further owners
 * can be created for that business day once the limit is hit.
 */
public class EnsureDailyOwnerLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate businessDay = BusinessDays.adjust(supplied != null ? supplied : LocalDate.now());
        long count = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException((int) count);
        }
    }
}
