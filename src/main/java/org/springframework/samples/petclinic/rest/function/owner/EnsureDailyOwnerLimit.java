package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once the maximum number of owners
 * ({@link DailyOwnerLimitException#LIMIT}) have already been created on the same business day,
 * counted by {@link Owner#getRegistrationDate()}. The day counted is this request's <em>adjusted</em>
 * business day: the effective registration date — supplied in the request or defaulted to the server
 * date — rolled forward off a weekend or public holiday to the next non-holiday business day,
 * matching what {@link BuildOwner} will store.
 * Runs before {@link SaveOwner}, so the new owner is not yet counted; a business day already holding
 * 100 owners causes the 101st request to fail with 429.
 */
public class EnsureDailyOwnerLimit {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        LocalDate businessDay = BusinessDays.rollForward(effective);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DailyOwnerLimitException.LIMIT) {
            throw new DailyOwnerLimitException(businessDay, count);
        }
    }
}
