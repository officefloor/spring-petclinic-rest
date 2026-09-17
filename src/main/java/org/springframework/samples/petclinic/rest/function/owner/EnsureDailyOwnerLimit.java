package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Create-owner step: rejects the request with a 429 when {@link #DAILY_LIMIT} or more owners
 * have already been registered on today's business day (by {@link Owner#getRegistrationDate()}).
 * The server date is rolled forward over weekends (see {@link BusinessDay}) so it matches the
 * adjusted registration dates the new and existing owners carry. Reads only the existing owners,
 * so it can run before the new owner is built.
 */
public class EnsureDailyOwnerLimit {

    /** The maximum number of owners that may be created on a single business day. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        if (OwnersCreatedToday.count(ownerRepository) >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DAILY_LIMIT);
        }
    }
}
