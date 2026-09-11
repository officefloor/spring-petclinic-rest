package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerDailyLimitExceededException;

/**
 * Rejects a create request once 100 or more owners have already been created today,
 * counting existing owners whose registration date is today. Runs after
 * {@link ValidateOwnerFields}, so it sees the validated request, and before
 * {@link BuildOwner}, so an over-limit request is a 429 rather than a persisted record.
 */
public class CheckOwnerDailyLimit {

    /** Reaching this many owners created today rejects further creates. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerDailyLimitExceededException {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        // Count against the adjusted business day this create will land on, so the
        // limit matches the registrationDate BuildOwner will store (weekend rolled
        // forward to Monday) rather than the raw supplied or server date.
        LocalDate businessDay = BusinessDay.adjust(effective);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new OwnerDailyLimitExceededException(count);
        }
    }
}
