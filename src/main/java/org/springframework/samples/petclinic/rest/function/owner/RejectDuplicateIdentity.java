package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request that lands in an existing household. Because the
 * {@code householdId} is now derived deterministically from (last name, postcode) (see
 * {@link AssignHousehold}), any existing owner sharing the new owner's {@code householdId}
 * <em>is</em> the same household, so a second such owner is a household duplicate and is
 * rejected 409 via {@link DuplicateIdentityException}.
 *
 * <p>{@code sharesHousehold} bypasses this block: the caller is declaring the owner a
 * genuine additional member of that household, so the create is allowed. (It no longer
 * creates any link — the shared identifier is implicit in the last name and postcode.)
 *
 * <p>Runs after {@link AssignHousehold} (so the new owner's {@code householdId} is set) and
 * within the create transaction, but before {@link SaveOwner}, so the new owner is not yet
 * persisted and cannot collide with itself.
 */
public class RejectDuplicateIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(
                        "Another owner with the same identity already exists");
            }
        }
    }
}
