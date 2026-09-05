package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request that shares a household with an existing owner: same
 * last name (compared case-insensitively with runs of whitespace collapsed to a single
 * space) and same address (compared in the normalized form of {@link AddressNormalizer}).
 * Runs before {@link BuildOwner}. A collision is rejected 409 via
 * {@link DuplicateHouseholdException}, unless the request opts in with
 * {@code sharesHousehold} set true.
 */
public class RejectDuplicateHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getLastName()).equals(lastName)
                    && AddressNormalizer.normalize(existing.getAddress()).equals(address)) {
                throw new DuplicateHouseholdException(
                        "Another owner with the same last name and address already exists");
            }
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
