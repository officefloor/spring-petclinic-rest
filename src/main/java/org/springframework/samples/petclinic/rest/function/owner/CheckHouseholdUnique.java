package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects (409) a create-owner request that would join an existing household. Because the household
 * is now keyed on the computed {@code householdId} (a pure function of the normalized last name and
 * postcode, see {@link OwnerIdentity#householdId}), owners sharing a last name and postcode are the
 * same household. So a second such owner is a household duplicate unless it declares
 * {@code sharesHousehold}, in which case this block is bypassed and it is created as a declared
 * household member.
 *
 * <p>Runs after {@link CheckIdentityUnique} (so an exact identity collision is still a 409 even when
 * {@code sharesHousehold} is set) and within the write transaction, comparing against the owners
 * persisted so far. A missing postcode carries no household, so it never collides here.
 */
public class CheckHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the block
        }
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode -> no household to collide with
        }
        String householdId = OwnerIdentity.householdId(request.getLastName(), postcode);
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateHouseholdException(householdId);
            }
        }
    }
}
