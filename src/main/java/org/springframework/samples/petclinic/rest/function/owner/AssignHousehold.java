package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create-owner request sets {@code sharesHousehold} true, the new owner is allowed to share a
 * household with the existing owner(s) at the same address and last name (see {@link HouseholdKey}).
 * This step assigns the new owner — and every matching existing owner that does not already carry one
 * — the same stable {@code householdId}, so the whole household shares one identifier.
 *
 * <p>Requests that do not set {@code sharesHousehold} leave the owner without a household id.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        HouseholdKey household = HouseholdKey.of(owner.getLastName(), owner.getAddress());
        String householdId = household.householdId();
        owner.setHouseholdId(householdId);

        // Backfill the existing household members so the whole household shares the identifier.
        // These are pre-existing side entities, not the request's new owner, so they are saved
        // directly here rather than through the pipeline's save: step (which persists only the
        // new owner mutated above).
        for (Owner existing : ownerRepository.findAll()) {
            if (household.matches(existing) && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}
