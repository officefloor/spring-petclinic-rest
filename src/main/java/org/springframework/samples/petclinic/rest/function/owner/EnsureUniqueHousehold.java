package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose last name and address already belong to an existing owner,
 * so the pair maps to a 409 — unless the request opts in with {@code sharesHousehold=true},
 * declaring that the two owners share a household. Comparison is by {@link HouseholdKey}, so
 * case and whitespace differences do not hide a collision.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String key = HouseholdKey.of(request.getLastName(), request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(HouseholdKey.of(existing.getLastName(), existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
