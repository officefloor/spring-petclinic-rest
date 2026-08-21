package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when the request opted in with {@code sharesHousehold: true}
 * and another owner already shares the same last name and address. The new owner and every existing
 * household member receive the same stable identifier (see {@link OwnerHousehold#id}), so joiners
 * converge on one value. Runs after {@link BuildOwner}, so the built owner is available to stamp.
 * When nobody else is in the household there is no one to share with, so the id is left null.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        boolean hasHousehold = false;
        String householdId = OwnerHousehold.id(request.getLastName(), request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerHousehold.sameHousehold(request.getLastName(), request.getAddress(),
                    existing.getLastName(), existing.getAddress())) {
                hasHousehold = true;
                // Back-fill members that predate the household so every member shares the id.
                if (existing.getHouseholdId() == null) {
                    existing.setHouseholdId(householdId);
                    ownerRepository.save(existing);
                }
            }
        }
        if (hasHousehold) {
            owner.setHouseholdId(householdId);
        }
    }
}
