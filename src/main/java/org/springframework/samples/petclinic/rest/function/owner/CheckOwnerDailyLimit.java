package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once 100 or more owners have already been created today,
 * throwing {@link DailyOwnerLimitException} for a 429. "Today" is counted by each stored owner's
 * {@code registrationDate} equalling {@link LocalDate#now()} (the same date {@link BuildOwner}
 * stamps on a new owner). Runs before the owner is saved, so the count excludes the owner being
 * created: the 100th owner of the day is accepted, the 101st is rejected.
 */
public class CheckOwnerDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException();
        }
    }
}
