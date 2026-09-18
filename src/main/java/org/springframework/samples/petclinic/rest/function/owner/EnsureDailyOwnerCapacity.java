package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Runs in {@code POST /api/owners} before any entity is built or persisted. Rejects the
 * request when the current day has already reached {@link #DAILY_LIMIT} owner registrations
 * (counted by {@code registrationDate}), throwing {@link DailyOwnerLimitException} for a 429.
 * Reads the count in the same transaction as the writes so the per-day cap is enforced
 * consistently.
 */
public class EnsureDailyOwnerCapacity {

    /** Maximum number of owners that may be registered in a single day. */
    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {

        LocalDate today = LocalDate.now();
        long todayCount = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (todayCount >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(today);
        }
    }
}
