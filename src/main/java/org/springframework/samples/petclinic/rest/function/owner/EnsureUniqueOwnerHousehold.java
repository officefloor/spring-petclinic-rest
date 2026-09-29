package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create whose lastName and address already belong to an existing owner,
 * comparing both case-insensitively with collapsed whitespace. A clash is rejected 409
 * via {@link DuplicateHouseholdException}, unless the request opts in with
 * {@code sharesHousehold} true.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        // Opt-in flag lets known housemates share a lastName and address.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-case and collapse whitespace runs to a single space for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
