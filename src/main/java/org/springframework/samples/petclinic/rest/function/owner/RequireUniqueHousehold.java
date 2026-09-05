package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that resolves to the same {@link HouseholdId} (derived
 * from lastName and postcode) as an existing owner, before {@link BuildOwner} runs. A
 * request may opt out by setting {@code sharesHousehold} true, which permits a deliberately
 * declared household member; the flag only bypasses this block and does not affect the
 * derived householdId. Requests without a postcode have no household and are left untouched.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String householdId = HouseholdId.of(request.getLastName(), postcode);
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateHouseholdException(request.getLastName(), postcode);
            }
        }
    }
}
