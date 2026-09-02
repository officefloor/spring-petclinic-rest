package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Records the id of an existing owner in the same household (same computed {@code householdId}, from
 * last name and postcode) that has a different telephone — a soft, non-blocking duplicate. A declared
 * household member ({@code sharesHousehold=true}) is not a suspected duplicate, so it is skipped, as
 * is an owner with no household. Runs before the owner is saved, so {@code findAll()} sees only
 * pre-existing owners. Leaves {@code possibleDuplicateOf} null when nothing matches.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        String telephone = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())
                    && !telephone.equals(existing.getTelephone())) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
