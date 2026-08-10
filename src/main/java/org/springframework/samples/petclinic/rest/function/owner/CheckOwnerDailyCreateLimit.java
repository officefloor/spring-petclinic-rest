package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyCreateLimitException;

/**
 * Runs before {@link BuildOwner}: rejects the request with 429 when 100 or more owners have
 * already been created today (by registrationDate) — the per-day create limit. Owners' effective
 * registrationDate is rolled forward off weekends to the next business day, so this counts against
 * the adjusted business day the new owner would be registered on.
 */
public class CheckOwnerDailyCreateLimit {

    private static final long DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws OwnerDailyCreateLimitException {
        LocalDate businessDay = BusinessDays.rollForward(LocalDate.now());
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        if (createdToday >= DAILY_LIMIT) {
            throw new OwnerDailyCreateLimitException(
                    "The daily limit of " + DAILY_LIMIT + " owner registrations has been reached");
        }
    }
}
