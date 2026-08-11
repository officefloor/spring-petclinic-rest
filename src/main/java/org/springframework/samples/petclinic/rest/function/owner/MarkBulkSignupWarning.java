package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the read-time {@code bulkSignupWarning} flag on an owner: {@code true} once more than
 * {@link #BULK_THRESHOLD} owners have already been registered on the current business day
 * (server date rolled forward off any weekend, matching {@link BusinessDay}), otherwise
 * {@code false}. Runs before the responder so the flag is carried onto the returned DTO.
 */
public class MarkBulkSignupWarning {

    /** The warning is raised once the count of owners created today exceeds this value. */
    public static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.rollForward(LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
