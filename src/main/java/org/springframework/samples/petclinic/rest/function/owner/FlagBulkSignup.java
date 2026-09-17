package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create request as a bulk signup: the owner's bulk-signup warning is set when more than
 * {@link #BULK_SIGNUP_THRESHOLD} owners have already been registered on its (business-day-adjusted)
 * registration date. Runs after {@link ResolveRegistrationDate} so it keys off the adjusted date,
 * and before {@link SaveOwner} so the count excludes the owner currently being created — the same
 * accumulation {@link EnsureDailyOwnerLimit} uses. Mutates the built {@link Owner} in place.
 */
public class FlagBulkSignup {

    /** A day is a bulk-signup day once more than this many owners are already registered on it. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long alreadyRegistered = ownerRepository.countRegisteredOn(owner.getRegistrationDate());
        owner.setBulkSignupWarning(alreadyRegistered > BULK_SIGNUP_THRESHOLD);
    }
}
