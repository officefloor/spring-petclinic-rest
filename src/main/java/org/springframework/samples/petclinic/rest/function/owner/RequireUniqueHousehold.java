package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Step of {@code POST /api/owners} that rejects a create when another owner already has the same
 * last name and address, throwing {@link DuplicateHouseholdException} (handled as 409 Conflict).
 * Last name and address are compared case-insensitively with collapsed whitespace. The check is
 * skipped when the request opts in with {@code sharesHousehold=true}. Runs after
 * {@link RequireOwnerFields} has published the validated body and before {@link BuildOwner}/
 * {@link SaveOwner} persist the new owner.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // owner explicitly shares a household with an existing owner
        }
        String lastName = Household.normalize(request.getLastName());
        String address = Household.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(Household.normalize(existing.getLastName()))
                    && address.equals(Household.normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(
                        "An owner with the same last name and address already exists");
            }
        }
    }
}
