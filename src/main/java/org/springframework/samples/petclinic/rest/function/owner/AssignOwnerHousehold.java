package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a shared {@code householdId} when a create request opts in with
 * {@code sharesHousehold: true}.
 *
 * <p>Runs after {@link BuildOwner} in the {@code POST /api/owners} pipeline. The id is derived
 * deterministically from the owner's normalized last name and address (see
 * {@link OwnerHousehold#householdId}), so every owner that joins the same household computes the
 * same value. The joining owner receives the id, and any existing owner in the same household that
 * does not yet carry one is backfilled with it — so both ends of a shared household end up with the
 * same stable identifier. When the request did not opt in, no id is assigned.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = OwnerHousehold.householdId(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);

        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getHouseholdId() == null
                    && OwnerHousehold.sameHousehold(existing, owner.getLastName(),
                            owner.getAddress())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}
