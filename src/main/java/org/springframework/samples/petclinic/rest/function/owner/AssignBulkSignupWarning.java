package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags a bulk signup. {@code bulkSignupWarning} is set to
 * {@code true} when, at the moment this owner is created, more than {@value #BULK_THRESHOLD} owners
 * have already been created for the same registration day (by the effective, business-day-adjusted
 * {@code registrationDate} from {@link DetermineRegistrationDate}); otherwise {@code false}. Runs
 * before {@link SaveOwner}, so the owner being created is not counted against itself.
 */
public class AssignBulkSignupWarning {

    /** Owners already created today above which a bulk-signup warning is flagged. */
    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val LocalDate registrationDate,
            OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
