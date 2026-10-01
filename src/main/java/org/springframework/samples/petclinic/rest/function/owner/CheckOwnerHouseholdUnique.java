package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdDuplicateException;

/**
 * Runs after {@link ValidateOwner} and before {@link BuildOwner}. Rejects the create with a 409 via
 * {@link OwnerHouseholdDuplicateException} when another owner already belongs to the same household
 * (same lastName and address — see {@link OwnerHousehold} for how that is compared).
 *
 * <p>The request may opt out of this rule by setting {@code sharesHousehold} true, which permits
 * creating a second owner in the same household; {@link AssignHousehold} then gives the new owner the
 * existing household's identifier.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // opted in to sharing a household; skip the duplicate check
        }
        OwnerHousehold household = OwnerHousehold.of(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (household.equals(OwnerHousehold.of(existing))) {
                throw new OwnerHouseholdDuplicateException(request.getLastName(), request.getAddress());
            }
        }
    }
}
