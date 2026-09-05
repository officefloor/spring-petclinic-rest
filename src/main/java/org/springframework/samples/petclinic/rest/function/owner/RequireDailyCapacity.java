package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyLimitException;

/**
 * Rejects a create-owner request once {@value #DAILY_LIMIT} or more owners have already been
 * registered today, before {@link BuildOwner} runs. Owners are counted by registrationDate,
 * which {@link BuildOwner} defaults to today for newly created owners.
 */
public class RequireDailyCapacity {

    static final int DAILY_LIMIT = 100;

    public void service(OwnerRepository ownerRepository) throws DailyLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitException(count);
        }
    }
}
