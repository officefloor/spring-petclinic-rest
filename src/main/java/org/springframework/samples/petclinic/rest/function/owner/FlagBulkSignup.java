package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: records on the owner whether it is being created on a
 * high-volume sign-up day. Sets {@code bulkSignupWarning} true when more than 80 owners have
 * already been created for this owner's registration date, false otherwise. Counts the owners
 * present before this create (the new owner is not yet saved) using the same per-day accumulation
 * as {@link RequireDailyCapacity}, and mutates the built owner in place before it is persisted.
 */
public class FlagBulkSignup {

    /** Owners already created today above which the response carries a bulk sign-up warning. */
    private static final int WARNING_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate registrationDate = owner.getRegistrationDate();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > WARNING_THRESHOLD);
    }
}
