package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ensures a newly built owner's {@code customerCode} does not collide with an existing owner's.
 * When it does, {@link CustomerCode#deduplicate} appends {@code -<n>} with the smallest {@code n >= 2}
 * that is free, and the de-duplicated code is stored back on the owner.
 *
 * <p>Runs after {@link AssignCustomerCode} has derived the code and before
 * {@link AssignMembershipNumber}, so every value built from the whole code sees the unique variant.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String unique = CustomerCode.deduplicate(owner.getCustomerCode(),
                code -> !ownerRepository.findByCustomerCode(code).isEmpty());
        owner.setCustomerCode(unique);
    }
}
