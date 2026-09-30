package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * The single duplicate check on create, keyed on the household. Because the household is now
 * keyed on {@code (lastName, postcode)} via a deterministic {@code householdId} (see
 * {@link OwnerHousehold}), owners sharing a lastName and postcode are the same household: a
 * second such owner is a household duplicate and is rejected 409 via
 * {@link DuplicateHouseholdException}.
 *
 * <p>{@code sharesHousehold} bypasses this block: a declared household member is allowed to be
 * created (and is not flagged as a possible duplicate downstream — see
 * {@link AssignPossibleDuplicate}). Runs after the householdId is assigned and before the owner
 * is saved.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member bypasses the duplicate block
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners do not block a create
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateHouseholdException(householdId);
            }
        }
    }
}
