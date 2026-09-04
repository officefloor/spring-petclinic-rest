package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The duplicate check for creating owners, now keyed on the household. The household is
 * {@code (normalizedLastName, postcode)} expressed as a deterministic {@code householdId} (see
 * {@link OwnerIdentityKey#householdId(String, String)}), so owners sharing a last name and postcode
 * are the same household. A create request whose computed household id matches an existing owner's is
 * rejected with 409 via {@link DuplicateOwnerException}.
 *
 * <p>Setting {@code sharesHousehold} true bypasses this block: the owner is then created as a declared
 * household member (and, being declared, is not flagged as a possible duplicate by
 * {@link AssignPossibleDuplicate}). Runs before {@link BuildOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the household duplicate block
        }
        String householdId = OwnerIdentityKey.householdIdOf(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(OwnerIdentityKey.householdIdOf(existing))) {
                throw new DuplicateOwnerException(householdId);
            }
        }
    }
}
