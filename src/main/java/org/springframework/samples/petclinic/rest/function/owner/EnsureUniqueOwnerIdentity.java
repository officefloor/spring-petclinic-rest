package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request that would be a second member of an existing household,
 * responding 409. The household is keyed on (lastName, postcode), so its deterministic
 * {@code householdId} (see {@link OwnerHousehold}) is shared by every owner with the same last
 * name and postcode; if any existing owner already carries that householdId, this request is a
 * household duplicate.
 *
 * <p>The {@code sharesHousehold} request flag bypasses this block: it no longer creates the
 * household link (the link is now automatic and deterministic) but declares that the caller
 * intends to join the existing household, so the owner is created as a declared household
 * member instead of being rejected. Such a declared member is later NOT flagged as a possible
 * duplicate (see {@link AssignOwnerPossibleDuplicate}).
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}; the request's
 * householdId is the same value {@link AssignOwnerHousehold} later stamps onto the owner. An
 * owner with no postcode has no household and is never a household duplicate.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the duplicate block
        }
        String householdId = OwnerHousehold.idFor(request.getLastName(), request.getPostcode());
        if (householdId == null) {
            return; // no postcode, so no household to collide with
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateOwnerIdentityException(householdId);
            }
        }
    }
}
