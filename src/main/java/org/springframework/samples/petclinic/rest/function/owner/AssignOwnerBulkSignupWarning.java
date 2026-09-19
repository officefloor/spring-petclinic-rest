package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code bulkSignupWarning}: {@code true} when more than
 * {@value #BULK_SIGNUP_THRESHOLD} owners have already been registered on this owner's
 * registration date, {@code false} otherwise. Runs after {@link BuildOwner} (so the
 * entity and its rolled registration date exist) and before {@link SaveOwner} persists
 * the flag, so the freshly created owner is not counted among the day's registrations.
 * Registrations are counted via {@link Registrations}, the same accumulation path the
 * daily creation limit uses.
 */
public class AssignOwnerBulkSignupWarning {

    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long alreadyCreated = Registrations.countOn(ownerRepository, owner.getRegistrationDate());
        owner.setBulkSignupWarning(alreadyCreated > BULK_SIGNUP_THRESHOLD);
    }
}
