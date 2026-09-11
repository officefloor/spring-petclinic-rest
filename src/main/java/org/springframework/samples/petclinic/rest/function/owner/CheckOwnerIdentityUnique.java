package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * The duplicate-detection block. Because the {@code householdId} is now derived
 * deterministically from {@code (lastName, postcode)} (see {@link AssignHousehold}), two
 * owners that share a last name and postcode are the same household. A second owner in an
 * existing household is therefore rejected as a household duplicate (409) — <em>unless</em>
 * the request opts in via {@code sharesHousehold}, which declares the new owner a member of
 * that household and bypasses the block.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} (so the new owner carries its
 * computed {@code householdId}) and before {@link SaveOwner} (so a duplicate is a 409 rather
 * than a persisted record).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new OwnerIdentityConflictException(householdId);
            }
        }
    }
}
