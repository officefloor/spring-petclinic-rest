package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once {@value DailyOwnerLimitException#DAILY_LIMIT} or more owners
 * have already been created today, counted by {@code registrationDate} against the server's current
 * date. A day at capacity is reported as 429 (see {@link DailyOwnerLimitException}).
 *
 * <p>Runs before {@link BuildOwner}, so a rejected request is never persisted. New owners default
 * their {@code registrationDate} to today (see {@link BuildOwner}), so this cap counts each day's
 * fresh registrations.
 */
public class CheckDailyOwnerLimit {

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DailyOwnerLimitException.DAILY_LIMIT) {
            throw new DailyOwnerLimitException(DailyOwnerLimitException.DAILY_LIMIT);
        }
    }
}
