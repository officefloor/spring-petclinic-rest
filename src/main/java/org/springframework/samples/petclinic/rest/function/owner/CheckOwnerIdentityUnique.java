package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * Household duplicate check for {@code POST /api/owners}. The household is keyed on
 * {@code (lastName, postcode)} via the deterministic {@code householdId} computed by
 * {@link AssignHousehold}, so any existing owner sharing the new owner's {@code householdId} is a
 * member of the same household. A second such owner is rejected with 409
 * ({@link OwnerIdentityConflictException}).
 *
 * <p>The {@code sharesHousehold} request flag bypasses this block: when set, the new owner is
 * created as a <em>declared</em> household member rather than rejected (and is not flagged a possible
 * duplicate — see {@link AssignPossibleDuplicate}). Runs after {@link AssignHousehold} (so the new
 * owner already carries its {@code householdId}) and before {@link SaveOwner} (so the new owner is
 * not yet in the repository scan).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                throw new OwnerIdentityConflictException(householdId);
            }
        }
    }
}
