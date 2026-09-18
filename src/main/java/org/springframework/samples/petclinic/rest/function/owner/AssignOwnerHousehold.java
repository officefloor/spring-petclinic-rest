package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs in {@code POST /api/owners} after the owner entity is built, only when the request
 * opted in with {@code sharesHousehold=true}. Assigns the built owner the stable
 * {@link Household#id(String, String) household id} for its lastName and address, and backfills
 * that same id onto any existing owner in the household so every member shares it. Runs within
 * the create transaction, before the owner is saved.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {

        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // only owners joining an existing household get a shared id
        }

        String householdId = Household.id(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);

        for (Owner existing : ownerRepository.findAll()) {
            if (Household.same(owner.getLastName(), owner.getAddress(),
                    existing.getLastName(), existing.getAddress())
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}
