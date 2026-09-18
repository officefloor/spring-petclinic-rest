package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Runs in {@code POST /api/owners} after the telephone-uniqueness check, reading the
 * already-validated body as a variable. Rejects the request when another owner already
 * shares its lastName and address (compared case-insensitively with collapsed whitespace),
 * throwing {@link DuplicateOwnerHouseholdException} for a 409 before any entity is built or
 * persisted. A request that opts in with {@code sharesHousehold=true} is allowed through.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // caller acknowledges the shared household
        }

        for (Owner existing : ownerRepository.findAll()) {
            if (Household.same(request.getLastName(), request.getAddress(),
                    existing.getLastName(), existing.getAddress())) {
                throw new DuplicateOwnerHouseholdException(request.getLastName());
            }
        }
    }
}
