package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.HouseholdDuplicateException;

/**
 * Enforces one owner per household on create. Because the household is keyed on (last name, postcode)
 * via the deterministic {@link Owner#getHouseholdId() householdId}, a second owner whose id already
 * belongs to an existing owner is a household duplicate and is rejected with 409 — unless the request
 * opts in with {@code sharesHousehold=true}, which bypasses the rejection and admits the owner as a
 * declared household member. In that case it publishes {@link DeclaredHouseholdMember} so
 * {@link DetectPossibleDuplicate} leaves the declared member unflagged.
 *
 * <p>Runs after {@link AssignHouseholdId} so the owner carries its final id, and before
 * {@link SaveOwner} so a rejection happens before anything is persisted. An owner without a postcode
 * belongs to no household and is always admitted.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository,
            Out<DeclaredHouseholdMember> declaredMember) throws HouseholdDuplicateException {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return;
        }
        boolean occupied = ownerRepository.findByLastName(owner.getLastName()).stream()
                .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (!occupied) {
            return;
        }
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            throw new HouseholdDuplicateException(householdId);
        }
        declaredMember.set(DeclaredHouseholdMember.INSTANCE);
    }
}
