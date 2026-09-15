package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create request opts in with {@code sharesHousehold=true} and there are existing
 * owners at the same household (same last name and address, compared case-insensitively
 * with collapsed whitespace), assigns the built owner and those existing members the same
 * stable {@code householdId}. Runs after {@link BuildOwner} and before {@link SaveOwner}
 * so the id is stored and returned with the new owner; existing members without one are
 * back-filled so the whole household shares it. A request that does not opt in, or that
 * has no existing house-mates, leaves the owner's {@code householdId} unset.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> members = Households.membersAt(ownerRepository, owner.getLastName(), owner.getAddress());
        if (members.isEmpty()) {
            return;
        }
        String householdId = Households.householdId(owner.getLastName(), owner.getAddress());
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        owner.setHouseholdId(householdId);
    }
}
