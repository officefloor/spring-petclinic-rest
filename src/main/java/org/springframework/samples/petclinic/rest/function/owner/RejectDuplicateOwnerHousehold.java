package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Collection;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects a create-owner request that would join an existing household, responding 409. The
 * household is keyed on last name and postcode (see {@link OwnerHouseholds}), so a second owner
 * with the same last name and postcode is a duplicate. The request can opt out of the block by
 * setting {@code sharesHousehold}: a declared household member is allowed through and is later
 * created as such (and is not treated as a suspected duplicate, see
 * {@link AssignOwnerPossibleDuplicate}).
 *
 * <p>Runs before {@link BuildOwner}, comparing the request's last name and postcode against the
 * existing owners; these fields are stored unchanged, so this matches what
 * {@link AssignOwnerHousehold} later stamps.
 */
public class RejectDuplicateOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is allowed past the duplicate block
        }
        Collection<Owner> existing = ownerRepository.findAll();
        if (!OwnerHouseholds.members(existing, request.getLastName(), request.getPostcode()).isEmpty()) {
            throw new DuplicateOwnerException(
                    OwnerHouseholds.householdId(request.getLastName(), request.getPostcode()));
        }
    }
}
