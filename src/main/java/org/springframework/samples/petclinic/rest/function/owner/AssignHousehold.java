package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create-owner request opts in with {@code sharesHousehold=true} and an existing owner already
 * occupies its household (same last name and address, per {@link Household}), gives the new owner and
 * every existing member one shared {@code householdId}: an id already held by a member is reused,
 * otherwise a stable one is derived and back-filled onto the existing members so all members agree.
 * Runs under the request transaction, after the owner entity is built.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findByLastName(request.getLastName())) {
            if (Household.matches(existing, request.getLastName(), request.getAddress())) {
                members.add(existing);
            }
        }
        if (members.isEmpty()) {
            return;
        }
        String householdId = members.stream()
                .map(Owner::getHouseholdId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElseGet(() -> Household.idFor(request.getLastName(), request.getAddress()));
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }
}
