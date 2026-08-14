package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Runs in the create-owner pipeline before {@link BuildOwner}. Rejects a request whose lastName and
 * address both match an existing owner's — compared case-insensitively with collapsed whitespace —
 * with a 409, so the same household is not registered twice. The request opts out of this check by
 * setting {@code sharesHousehold} true, which permits deliberately sharing a household.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // caller has explicitly opted into sharing a household
        }
        String lastName = canonical(request.getLastName());
        String address = canonical(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(canonical(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Trim, collapse internal whitespace runs to a single space and lower-case for comparison. */
    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
