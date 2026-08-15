package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that rejects the request when {@value #MAX_OWNERS_PER_DAY} or
 * more owners have already been created for this request's business day, counted by {@link
 * Owner#getRegistrationDate()}, so a full day is a 429 Too Many Requests rather than an over-quota
 * create. The day counted is the adjusted business day resolved by {@link ResolveRegistrationDate}
 * (weekends roll forward to Monday), the same date {@link BuildOwner} will stamp on the new owner,
 * so the count reflects only owners already persisted against that business day.
 */
public class RejectDailyOwnerLimit {

    /** Maximum number of owners that may be created in a single day. */
    static final int MAX_OWNERS_PER_DAY = 100;

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        long count = ownerRepository.findAll().stream()
                .map(Owner::getRegistrationDate)
                .filter(registrationDate::equals)
                .count();
        if (count >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitException((int) count);
        }
    }
}
