package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Links owners that share a household. When the request opts in with {@code sharesHousehold}
 * true and existing owners share its household (see {@link OwnerHouseholds}), assigns the
 * built owner and those existing members the same stable {@code householdId}. Runs after the
 * owner is built and mutates it in place before it is saved; requests that do not opt in are
 * left untouched.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> members = OwnerHouseholds.members(ownerRepository.findAll(),
                owner.getLastName(), owner.getAddress());
        if (members.isEmpty()) {
            return;
        }
        String householdId = OwnerHouseholds.householdId(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }
}
