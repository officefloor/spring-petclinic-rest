package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Runs in the create-owner pipeline before {@link BuildOwner}. Counts existing owners registered
 * today (by registrationDate) and rejects the request with a 429 once that count has reached the
 * daily limit of 100, so no further owners can be created today once the limit is hit.
 */
public class EnsureDailyOwnerLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException((int) count);
        }
    }
}
