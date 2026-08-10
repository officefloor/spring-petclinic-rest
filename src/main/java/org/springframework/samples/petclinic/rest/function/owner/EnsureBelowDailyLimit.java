package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.util.BusinessDay;

/**
 * Runs before {@link BuildOwner}. Rejects the request when 100 or more owners have already been
 * created for the same business day, by throwing {@link DailyOwnerLimitException} (handled as 429 Too
 * Many Requests). The effective registration date (supplied on the request or defaulted to the server
 * date) is rolled forward onto a business day via {@link BusinessDay}, matching how the owner is
 * stored, and existing owners are counted by that adjusted {@code registrationDate}.
 */
public class EnsureBelowDailyLimit {

    /** The maximum number of owners permitted to be created for a single business day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate requested = request.getRegistrationDate();
        LocalDate businessDay = BusinessDay.adjust(requested != null ? requested : LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
