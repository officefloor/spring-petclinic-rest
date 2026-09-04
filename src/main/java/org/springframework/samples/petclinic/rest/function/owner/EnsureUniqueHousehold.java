package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create request whose last name and address both match an existing owner's, so two owners
 * cannot share a household by accident. Last name and address are compared case-insensitively with
 * collapsed whitespace (leading/trailing trimmed, internal runs reduced to a single space). Runs
 * before {@link BuildOwner}; a match is a 409 via {@link DuplicateHouseholdException}. A request that
 * explicitly sets {@code sharesHousehold} true opts out of this check and is allowed.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getLastName()).equals(lastName)
                    && normalize(existing.getAddress()).equals(address)) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
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
