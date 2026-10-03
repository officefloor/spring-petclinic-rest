package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners
 * have already been created on the new owner's effective business day, otherwise
 * {@code false}. The business day is resolved through {@link BusinessDays} (the same
 * single source of truth {@link EnsureDailyLimit} uses) and the count comes from
 * {@link BusinessDays#countCreatedOn}, so the two per-day rules cannot drift apart. The
 * count is over the existing owners (the new owner is not yet saved); the threshold is
 * strictly more than 80.
 */
public class AssessBulkSignup {

    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = BusinessDays.effectiveRegistrationDate(owner.getRegistrationDate());
        long createdToday = BusinessDays.countCreatedOn(ownerRepository, businessDay);
        owner.setBulkSignupWarning(createdToday > BULK_SIGNUP_THRESHOLD);
    }
}
