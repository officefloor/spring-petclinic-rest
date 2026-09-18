package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in {@code POST /api/owners} after the owner entity is built and before it is saved,
 * within the create transaction. Flags the built owner with a bulk-signup warning when more
 * than {@link #BULK_SIGNUP_THRESHOLD} owners had already been created for this owner's resolved
 * registration day. Because the new owner has not yet been persisted,
 * {@link OwnerRepository#countRegisteredOn(LocalDate)} counts only the owners created before
 * this one, so the flag reflects the day's volume without counting the owner itself.
 */
public class AssignOwnerBulkSignupWarning {

    /** Owners already created on the day above which the warning is raised. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val LocalDate registrationDate,
            OwnerRepository ownerRepository) {
        long dayCount = ownerRepository.countRegisteredOn(registrationDate);
        owner.setBulkSignupWarning(dayCount > BULK_SIGNUP_THRESHOLD);
    }
}
