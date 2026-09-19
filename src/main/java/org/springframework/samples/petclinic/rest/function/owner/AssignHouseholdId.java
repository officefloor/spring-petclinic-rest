package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Links owners who share a household. When a create-owner request opts in with
 * {@code sharesHousehold} true and an existing same-last-name, same-address owner is
 * present, assigns the shared, stable {@code householdId} (see {@link Households}) to both
 * the new owner and every existing household member. Runs after {@link BuildOwner} so the
 * new entity exists, and before {@link SaveOwner} persists it.
 */
public class AssignHouseholdId {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> members = Households.membersOf(ownerRepository, request.getLastName(), request.getAddress());
        if (members.isEmpty()) {
            return;
        }
        String householdId = Households.idFor(request.getLastName(), request.getAddress());
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }
}
