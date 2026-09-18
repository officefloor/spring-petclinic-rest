package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Resolves whether the owner response should carry a bulk-signup warning and publishes it for the
 * responder. The warning is raised once more than {@value #BULK_SIGNUP_THRESHOLD} owners have
 * already been registered on the current date, matching the per-day accumulation counted by
 * {@link EnsureDailyOwnerLimit}. Runs before the responder so both the create and read responses
 * carry the flag.
 */
public class ResolveBulkSignupWarning {

    /** Owners registered today beyond this count raise the bulk-signup warning. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(OwnerRepository ownerRepository, Out<Boolean> bulkSignupWarning) {
        long count = ownerRepository.countByRegistrationDate(LocalDate.now());
        bulkSignupWarning.set(count > BULK_SIGNUP_THRESHOLD);
    }
}
