package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Step of {@code POST /api/owners} that consolidates duplicate detection. Runs after
 * {@link RequireOwnerFields} has normalized the body and rejects the create with 409 when either:
 *
 * <ul>
 * <li>the new owner's WHOLE identityKey (normalizedTelephone + '|' + email + '|' + householdId)
 * equals an existing owner's — an exact identity match; or</li>
 * <li>an existing owner shares the new owner's household (same deterministic householdId, i.e. the
 * same lastName and postcode) — a household duplicate. This block is bypassed when the request sets
 * {@code sharesHousehold=true}: the owner is then created as a declared household member.</li>
 * </ul>
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentityKey.forRequest(request);
        String householdId = AssignHouseholdId.forRequest(request);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
