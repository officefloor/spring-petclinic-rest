package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block: because a household is keyed on (last name, postcode), an existing owner that
 * shares the new owner's {@link HouseholdNormalizer#id(String, String) household id} is the same
 * household, so the request is a household duplicate and is rejected with a 409 via
 * {@link DuplicateIdentityException}. Setting {@code sharesHousehold} acknowledges the shared
 * household and bypasses this block, so the new owner is created as a declared household member. Runs
 * after {@link AssignHousehold}, so the built owner already carries its household id. Soft-deleted
 * owners are ignored, so a match that has since been deleted no longer blocks the create.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isActive() && HouseholdNormalizer.belongsTo(existing, householdId)) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
