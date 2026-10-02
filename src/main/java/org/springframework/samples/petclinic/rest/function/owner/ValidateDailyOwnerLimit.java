package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Runs after {@link ValidateCityCapacity} and before {@link BuildOwner}. A read-only
 * validation gate: rejects the create with a 429 when 100 or more owners have already been
 * created on the same business day (by {@code registrationDate}), so no more than 100 owners
 * can be registered per business day. The business day is this create's effective,
 * weekend-adjusted registration date — matching the date {@link BuildOwner} will store — so
 * the count buckets owners by adjusted business day. Publishes nothing; it only escalates
 * when the limit is reached.
 */
public class ValidateDailyOwnerLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate businessDay = BusinessDay.effectiveRegistrationDate(request.getRegistrationDate());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(
                    DAILY_LIMIT + " or more owners have already been created on this business day");
        }
    }
}
