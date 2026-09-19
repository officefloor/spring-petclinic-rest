package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.HouseholdDuplicateException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request that would silently join an existing household. A household is keyed
 * on (last name, postcode) via {@link HouseholdKey}, so an existing owner sharing this request's
 * {@link HouseholdKey#id(String, String) household id} means the same household. Such a request is
 * a household duplicate and maps to a 409 — unless it declares {@code sharesHousehold}, in which
 * case the owner is knowingly joining the household and is allowed through (created as a declared
 * member by the later steps, and not flagged as a possible duplicate).
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link BuildOwner} maps the body to an
 * entity. Existing owners' household ids are recomputed from their last name and postcode, so a
 * match does not depend on any stored value. A household needs a postcode: when the request has
 * none, no household can be asserted and the block does not apply.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws HouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String householdId = HouseholdKey.id(request.getLastName(), postcode);
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(HouseholdKey.id(existing.getLastName(), existing.getPostcode()))) {
                throw new HouseholdDuplicateException(householdId, existing.getId());
            }
        }
    }
}
