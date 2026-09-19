package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create as part of a potential bulk-signup surge: when more than
 * {@link #BULK_WARNING_THRESHOLD} owners have already been registered on the new owner's
 * effective business day, {@code bulkSignupWarning} is set true, otherwise false. Runs
 * after {@link ResolveRegistrationDate} has stored the adjusted date and before
 * {@link SaveOwner} persists the entity, so the count sees only the owners that existed
 * before this create; the flag is stored on the owner and returned on every later read.
 * This is a soft warning below the hard cap enforced by {@link EnsureDailyLimit}.
 */
public class FlagBulkSignup {

    /** Warn once strictly more than this many owners are already registered on the day. */
    static final int BULK_WARNING_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = DailyRegistrations.countOn(ownerRepository, owner.getRegistrationDate());
        owner.setBulkSignupWarning(count > BULK_WARNING_THRESHOLD);
    }
}
