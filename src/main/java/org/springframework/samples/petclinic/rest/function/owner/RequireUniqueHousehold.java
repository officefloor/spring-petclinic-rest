package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Step of {@code POST /api/owners} that rejects a create with 409 when another owner already
 * has the same lastName and the same address, comparing both case-insensitively with collapsed
 * whitespace. Skipped when the request sets {@code sharesHousehold=true}, so people who
 * explicitly share a household can still be registered.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
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

    /** Trim, collapse internal whitespace runs to a single space, and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
