package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitException;

/**
 * Rejects a create request once 100 or more owners have already been registered on the current
 * business day, so the 101st registration of the day is a 429. Existing owners are counted by
 * registrationDate, compared against the business-day-adjusted current date (weekends roll forward
 * to Monday, matching the effective registration date new owners receive).
 */
public class EnsureDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitException {
        LocalDate today = BusinessDays.adjust(LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitException(DAILY_LIMIT);
        }
    }
}
