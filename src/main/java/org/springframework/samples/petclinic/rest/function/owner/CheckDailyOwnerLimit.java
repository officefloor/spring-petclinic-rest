package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs on {@code POST /api/owners} before {@link BuildOwner} (so no owner is created on rejection):
 * rejects the request with 429 via {@link DailyOwnerLimitException} when {@link #MAX_OWNERS_PER_DAY}
 * or more owners have already been registered on the current business day (by {@code
 * registrationDate}). Because a weekend registration date rolls forward to the next Monday (see
 * {@link BusinessDay}), the bucket is that adjusted business day, not the raw calendar day.
 */
public class CheckDailyOwnerLimit {

    /** Maximum owners that may be registered in a single day; the next create is rejected. */
    static final int MAX_OWNERS_PER_DAY = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = BusinessDay.rollForward(LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate()) && ++count >= MAX_OWNERS_PER_DAY) {
                throw new DailyOwnerLimitException(MAX_OWNERS_PER_DAY);
            }
        }
    }
}
