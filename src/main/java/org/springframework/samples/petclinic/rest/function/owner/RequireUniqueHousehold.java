package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects a create request that shares another owner's household: same last name and address,
 * compared case-insensitively with collapsed whitespace. A request with {@code sharesHousehold}
 * true opts out of the check. A match rejects 409.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner owner : ownerRepository.findAll()) {
            if (lastName.equals(normalize(owner.getLastName()))
                    && address.equals(normalize(owner.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
