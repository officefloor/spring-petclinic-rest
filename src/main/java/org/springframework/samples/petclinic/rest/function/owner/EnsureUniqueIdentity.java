package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block: a request is a hard duplicate when an existing active owner shares both the
 * new owner's {@link HouseholdNormalizer#id(String, String) household id} (same last name and
 * postcode) and its telephone, and is rejected with a 409 via {@link DuplicateIdentityException}. A
 * household member giving a <em>different</em> telephone is not a hard duplicate: it passes this
 * block and is instead marked for follow-up by {@link FlagPossibleDuplicate} and held to its
 * household level ceiling by {@link CapMembershipLevel}. Setting {@code sharesHousehold} acknowledges
 * the shared household and bypasses this block outright, so the new owner is created as a declared
 * household member. Runs after {@link AssignHousehold}, so the built owner already carries its
 * household id. Soft-deleted owners are ignored, so a match that has since been deleted no longer
 * blocks the create.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isActive() && HouseholdNormalizer.belongsTo(existing, householdId)
                    && owner.getTelephone().equals(existing.getTelephone())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
