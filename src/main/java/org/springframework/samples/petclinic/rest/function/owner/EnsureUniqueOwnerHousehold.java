package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

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

        String lastName = canonical(request.getLastName());
        String address = canonical(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(canonical(existing.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(request.getLastName());
            }
        }
    }

    /** Case-insensitive form with leading/trailing and repeated internal whitespace collapsed. */
    private static String canonical(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
