package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Create-owner step: the duplicate block. Owners sharing a last name and postcode are the
 * same {@link Household}, so a request whose computed {@code householdId} already belongs to
 * an existing owner is rejected with a 409 — unless it opts in with {@code sharesHousehold},
 * declaring itself a genuine additional member of that household.
 *
 * <p>Runs after {@link AssignHousehold} (so the new owner already carries its final
 * {@code householdId}) and before {@link FlagPossibleDuplicate} and {@link SaveOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member bypasses the duplicate block
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return; // no postcode -> no household -> nothing to collide with
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer occupies its household
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
