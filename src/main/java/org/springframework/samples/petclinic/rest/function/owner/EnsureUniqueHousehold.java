package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.HouseholdDuplicateException;

/**
 * Rejects a create-owner request that would join an existing owner's household — the same
 * computed {@code householdId} (derived from last name and postcode by
 * {@link AssignHousehold}) — responding 409 via {@link HouseholdDuplicateException}.
 *
 * <p>The household is keyed on {@code (lastName, postcode)}, so a second owner sharing
 * that pair is the same household. Setting {@code sharesHousehold: true} bypasses this
 * block: such an owner is created as a <em>declared</em> household member (and is not
 * flagged as a possible duplicate — see {@link AssignPossibleDuplicate}). A full-identity
 * duplicate is still caught afterwards by {@link EnsureUniqueIdentity}, so even a declared
 * member that duplicates an existing owner's whole {@code identityKey} is rejected.
 *
 * <p>Runs after {@link AssignHousehold} (so the built owner already carries its computed
 * {@code householdId}) and before {@link EnsureUniqueIdentity}/{@link SaveOwner}. The new
 * owner is not yet in the repository, so it is never compared against itself.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) throws HouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared member bypasses the household-duplicate block
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                throw new HouseholdDuplicateException(householdId);
            }
        }
    }
}
