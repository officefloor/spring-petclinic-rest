package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitExceededException;

/**
 * Rejects the create when 100 or more owners have already been created on the same business day,
 * counted by {@link Owner#getRegistrationDate() registrationDate} equal to this request's effective
 * business day. The effective date is the supplied (or defaulted) registration date rolled forward
 * off any weekend, matching what {@link BuildOwner} will store. The daily quota is full, so no
 * further owner may be created for that day; it is rejected 429 via
 * {@link OwnerDailyLimitExceededException}.
 *
 * <p>Runs before {@link SaveOwner}, so the count reflects only the existing owners, not the
 * one being created.
 */
public class RequireDailyOwnerLimit {

    /** At most this many owners may be created on any one business day. */
    static final long DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerDailyLimitExceededException {
        LocalDate businessDay = RegistrationDate.effective(request);
        long createdThatDay = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        if (createdThatDay >= DAILY_LIMIT) {
            throw new OwnerDailyLimitExceededException(businessDay, createdThatDay);
        }
    }
}
