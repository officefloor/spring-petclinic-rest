package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitException;

/**
 * Runs after {@link ValidateOwner} and before {@link BuildOwner}. Rejects the create with a 429 via
 * {@link OwnerDailyLimitException} when the maximum number of owners ({@value #MAX_OWNERS_PER_DAY})
 * already share the new owner's effective registration date, as determined by their
 * {@code registrationDate}. That effective date is the request's supplied date, or the server's
 * current date when omitted, rolled forward off any weekend to a business day (see {@link BusinessDay}),
 * so the limit counts owners per adjusted business day. The new owner is still unsaved — and is itself
 * dated that same business day (see {@link BuildOwner}) — so it is not counted against the limit.
 */
public class CheckOwnerDailyLimit {

    /** At most this many owners may be created in a single day; the create of one more is rejected. */
    static final int MAX_OWNERS_PER_DAY = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerDailyLimitException {
        LocalDate businessDay = BusinessDay.effective(request.getRegistrationDate());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new OwnerDailyLimitException(businessDay, count);
        }
    }
}
