package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create request once the daily signup limit has been reached.
 *
 * <p>Runs after {@link CheckOwnerCityCapacity} and before {@link BuildOwner}. The clinic accepts at
 * most 100 new owners per day: when 100 or more owners already carry today's registration date, the
 * next create is refused. A match throws {@link OwnerDailyLimitReachedException}, handled as a 429
 * Too Many Requests.
 */
public class CheckOwnerDailyLimit {

    /** Maximum number of owners that may be created on a single day; the next owner is rejected. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerDailyLimitReachedException {
        // Count against the business day this owner will actually be registered on. Stored
        // registration dates are already adjusted, so this buckets owners per adjusted business day.
        LocalDate businessDay = BusinessDays.effectiveRegistrationDate(request.getRegistrationDate());
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        if (createdToday >= DAILY_LIMIT) {
            throw new OwnerDailyLimitReachedException(businessDay);
        }
    }
}
