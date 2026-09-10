package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's {@code bulkSignupWarning} flag to true when more than
 * {@value #BULK_SIGNUP_THRESHOLD} owners share its registration date (i.e. more than
 * that many owners were created for that business day), otherwise false. Uses the same
 * per-day accumulation as {@link EnsureDailyCreateLimit}. Runs on both the create
 * pipeline (after Save, so the new owner is counted) and the read pipeline, so the flag
 * is identical whether returned from the create response or a later GET.
 */
public class AssignOwnerBulkSignupWarning {

    /** Owners created for a single day beyond which the bulk-signup warning is raised. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate day = owner.getRegistrationDate();
        long count = day == null ? 0 : ownerRepository.findAll().stream()
                .filter(o -> day.equals(o.getRegistrationDate()))
                .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
