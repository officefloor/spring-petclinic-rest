package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that shares both lastName and address with an existing
 * owner, before {@link BuildOwner} runs. Names and addresses are compared case-insensitively
 * with collapsed whitespace. A request may opt out by setting {@code sharesHousehold} true,
 * which permits deliberately co-resident owners (e.g. members of the same household).
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalise(request.getLastName());
        String address = normalise(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalise(existing.getLastName()))
                    && address.equals(normalise(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
