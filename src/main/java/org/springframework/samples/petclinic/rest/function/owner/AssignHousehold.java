package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-owner step for the {@code sharesHousehold} opt-in: when the request joins an
 * existing {@link Household} (another owner with the same last name and address), assigns
 * the shared household identifier to both the new owner and every existing member, so all
 * owners in the household report the same {@code householdId}. A no-op when the request
 * does not opt in or joins no existing owner. Runs after {@link BuildOwner} and before
 * {@link SaveOwner} persists the new owner.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = Household.id(owner.getLastName(), owner.getAddress());
        boolean joined = false;
        for (Owner member : ownerRepository.findAll()) {
            if (Household.matches(member, owner.getLastName(), owner.getAddress())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
                joined = true;
            }
        }
        if (joined) {
            owner.setHouseholdId(householdId);
        }
    }
}
