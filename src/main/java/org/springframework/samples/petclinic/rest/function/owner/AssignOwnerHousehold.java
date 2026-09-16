package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create-owner request opts in with {@code sharesHousehold} true, assigns the new
 * owner and every existing owner in the same household — same last name and address once
 * normalized (see {@link OwnerHouseholds}) — the same stable {@code householdId}. A no-op
 * for a request that does not opt in.
 *
 * <p>Runs after {@link BuildOwner}, so it works with the built {@link Owner} entity and
 * relies on {@link RequireUniqueHousehold} having already allowed the shared household.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String key = OwnerHouseholds.key(owner.getLastName(), owner.getAddress());
        String householdId = OwnerHouseholds.id(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(OwnerHouseholds.key(existing.getLastName(), existing.getAddress()))) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}
