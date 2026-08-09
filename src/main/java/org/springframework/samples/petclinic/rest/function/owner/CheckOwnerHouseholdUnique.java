package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdConflictException;

/**
 * Rejects a create-owner request that would share a household with an existing owner — the same
 * lastName (compared case-insensitively with collapsed whitespace) and the same address, compared
 * in normalized form (see {@link OwnerAddress#normalize(String)}). Throws
 * {@link OwnerHouseholdConflictException} (handled as 409) on a match, unless the request opted in
 * with {@code sharesHousehold: true}, in which case the duplicate is allowed.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdConflictException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = OwnerAddress.forComparison(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(OwnerAddress.forComparison(existing.getAddress()))) {
                throw new OwnerHouseholdConflictException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
