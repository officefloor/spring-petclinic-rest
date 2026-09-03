package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate check for {@code POST /api/owners}, keyed on the household. The household id is now
 * deterministic — derived from the normalized lastName and postcode (see
 * {@link OwnerIdentity#deriveHouseholdId}) — so owners that share a lastName and postcode are the same
 * household. A second owner in an existing household is therefore rejected as a household duplicate
 * with 409, <em>unless</em> the request sets {@code sharesHousehold}, which bypasses this block and
 * lets the owner be created as a declared household member. On a match it throws
 * {@link DuplicateIdentityException}, handled globally as 409.
 */
public class EnsureIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        // A declared household member is allowed to join an existing household, so it bypasses the
        // duplicate block entirely.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }

        String householdId = OwnerIdentity.householdIdOf(request.getLastName(), request.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            // A soft-deleted owner no longer blocks a create.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            String existingHousehold = OwnerIdentity.householdIdOf(
                    existing.getLastName(), existing.getPostcode());
            if (householdId.equals(existingHousehold)) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
