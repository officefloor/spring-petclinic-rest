package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Stamps {@code bulkSignupWarning} onto the newly built owner: {@code true} once more than
 * {@value #BULK_SIGNUP_THRESHOLD} owners have already been created for this owner's registration
 * business day, otherwise {@code false}. Runs before {@link SaveOwner}, so the new owner is not yet
 * persisted and therefore not counted among the existing owners.
 */
public class FlagOwnerBulkSignup {

    /** Once more than this many owners already carry the registration day, the warning is raised. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
                .map(Owner::getRegistrationDate)
                .filter(owner.getRegistrationDate()::equals)
                .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
