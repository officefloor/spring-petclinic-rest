package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyCreateLimitException;

/**
 * Runs before {@link BuildOwner}: rejects the request with 429 when 100 or more owners have
 * already been created today (by registrationDate) — the per-day create limit. Owners default
 * their registrationDate to the current date at creation, so this counts today's registrations.
 */
public class CheckOwnerDailyCreateLimit {

    private static final long DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws OwnerDailyCreateLimitException {
        LocalDate today = LocalDate.now();
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (createdToday >= DAILY_LIMIT) {
            throw new OwnerDailyCreateLimitException(
                    "The daily limit of " + DAILY_LIMIT + " owner registrations has been reached");
        }
    }
}
