package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * The duplicate check for creating an owner: rejects 409 when the new owner would join an existing
 * household — that is, when an existing owner shares the new owner's computed
 * {@link Owner#getHouseholdId() householdId} (owners with the same lastName and postcode are the
 * same household). Because the household is keyed on (lastName, postcode), a second such owner is a
 * household duplicate.
 *
 * <p>The request's {@code sharesHousehold} flag is the sole bypass: when set, the owner is being
 * created as a <em>declared</em> household member, so the block is skipped and the owner is created
 * despite the collision. It no longer creates the household link — the link is implicit in the
 * shared {@code householdId} assigned by {@link AssignOwnerHousehold}.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignOwnerHousehold} have produced the entity and
 * resolved its {@code householdId}. The new owner is not yet persisted, so
 * {@link OwnerRepository#findAll()} yields only the other owners.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateHouseholdException(
                        "An owner in the same household already exists");
            }
        }
    }
}
