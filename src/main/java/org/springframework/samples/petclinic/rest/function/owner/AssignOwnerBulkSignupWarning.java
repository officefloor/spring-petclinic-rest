package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create as a bulk signup when more than {@link #BULK_SIGNUP_THRESHOLD} owners have
 * already been registered on the new owner's business day, so the response carries
 * {@code bulkSignupWarning}. Counts against the same registration day as
 * {@link RejectDailyOwnerLimit} via {@link OwnerRegistrationDays}, so the two rules see the day
 * identically. Runs after {@link BuildOwner} but before the owner is saved, so the count reflects
 * the owners that existed before this create, and mutates the built {@link Owner} in place.
 */
public class AssignOwnerBulkSignupWarning {

    /** Owners already registered on a day above which a further signup is warned as bulk. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val LocalDate registrationDate,
            OwnerRepository ownerRepository) {
        int alreadyRegistered = OwnerRegistrationDays.size(ownerRepository.findAll(), registrationDate);
        owner.setBulkSignupWarning(alreadyRegistered > BULK_SIGNUP_THRESHOLD);
    }
}
