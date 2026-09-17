package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Create-owner step: rejects the request with a 409 when another owner already shares the
 * same {@link Household} — the same last name and address. Skipped when the request opts
 * in with {@code sharesHousehold: true}, in which case {@link AssignHousehold} instead
 * links the owners under a shared identifier. Runs after {@link ValidateOwnerFields} (so
 * the required fields are present) and before {@link BuildOwner}.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            if (Household.matches(owner, request.getLastName(), request.getAddress())) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
