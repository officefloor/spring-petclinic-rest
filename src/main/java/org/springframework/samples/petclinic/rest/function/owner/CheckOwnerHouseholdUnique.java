package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request that would be a second owner in an existing household.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. A household is keyed on
 * {@code (lastName, postcode)} through the deterministic {@link OwnerHousehold#householdId}, so an
 * owner whose computed household id already belongs to another owner is a household duplicate and is
 * rejected with {@link OwnerHouseholdDuplicateException} (409 Conflict).
 *
 * <p>The {@code sharesHousehold} flag bypasses this block: when the request opts in, the owner is
 * created as a declared member of the household instead of being rejected. An owner with no postcode
 * belongs to no household (its household id is {@code null}) and so is never a household duplicate.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        String householdId = OwnerHousehold.householdId(request.getLastName(), request.getPostcode());
        if (householdId == null) {
            return; // no postcode means no household, so no household duplicate is possible
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the duplicate check
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new OwnerHouseholdDuplicateException(householdId);
            }
        }
    }
}
