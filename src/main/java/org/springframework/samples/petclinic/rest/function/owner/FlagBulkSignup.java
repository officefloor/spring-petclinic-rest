package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the response with {@code bulkSignupWarning}: {@code true} when more than
 * {@link RegistrationDates#BULK_WARNING_THRESHOLD} owners have already been created for this owner's
 * registration day, otherwise {@code false}. The day is the owner's stored (business-day adjusted)
 * registration date, and the count is taken over all owners sharing that date via
 * {@link RegistrationDates#countOn} — the same accumulation path as {@link CheckDailyLimit}. Computed
 * per request and mutated onto the loaded owner in place, not persisted.
 */
public class FlagBulkSignup {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = RegistrationDates.countOn(owner.getRegistrationDate(), ownerRepository);
        owner.setBulkSignupWarning(count > RegistrationDates.BULK_WARNING_THRESHOLD);
    }
}
