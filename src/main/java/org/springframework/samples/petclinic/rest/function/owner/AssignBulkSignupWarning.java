package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: true when more than
 * {@link #BULK_SIGNUP_THRESHOLD} owners have already been created for the same
 * (business-day-adjusted) registration date, before this create. Runs after
 * {@link BuildOwner} (so the effective {@code registrationDate} is set) and before
 * {@link SaveOwner} (so the new owner is not counted), mirroring the accumulation path
 * used by {@link RequireDailyLimit}.
 */
public class AssignBulkSignupWarning {

    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
            .filter(existing -> owner.getRegistrationDate() != null
                && owner.getRegistrationDate().equals(existing.getRegistrationDate()))
            .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
