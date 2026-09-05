package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.rest.escalation.DailyLimitExceededException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request once 100 or more owners have already been registered
 * today (by {@code registrationDate}). Runs before {@link BuildOwner}, counting existing
 * owners whose registration date is today; at the limit the request is rejected 429 via
 * {@link DailyLimitExceededException}. New owners default to today's registration date
 * (see {@link BuildOwner}), so this cap governs same-day signups.
 */
public class RejectDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitExceededException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
                .filter(o -> today.equals(o.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitExceededException(
                    "The maximum number of owners for today has already been reached");
        }
    }
}
