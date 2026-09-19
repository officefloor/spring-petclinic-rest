package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The duplicate block: rejects a create-owner request that would repeat an existing owner in
 * a household — same last name and postcode (hence the same derived {@code householdId}, see
 * {@link Households}) and the same telephone — responding 409. A different person in the same
 * household (same household, different telephone) is a legitimate additional member: it is
 * created anyway and flagged by {@link AssignOwnerPossibleDuplicate}. A request opting in with
 * {@code sharesHousehold} declares the new owner a genuine member and bypasses the block
 * entirely. An owner with no postcode has no household and is never blocked here. Runs after
 * {@link AssignHouseholdId} (so the household id is set) and before {@link SaveOwner} persists
 * a duplicate.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (owner.getHouseholdId() == null || Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        boolean sameTelephoneMember = Households.membersOf(ownerRepository, owner.getLastName(), owner.getPostcode())
                .stream()
                .anyMatch(existing -> owner.getTelephone().equals(existing.getTelephone()));
        if (sameTelephoneMember) {
            throw new DuplicateHouseholdException(owner.getHouseholdId());
        }
    }
}
