package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Runs after {@link ValidateUniqueTelephone} and before {@link BuildOwner}. Rejects the create
 * with a 409 when another owner already shares the same household — the same last name at the
 * same address, compared case-insensitively with collapsed whitespace — unless the request
 * opts in with {@code sharesHousehold = true}. Publishes nothing; it is a validation gate that
 * only escalates on conflict.
 */
public class ValidateUniqueHousehold {

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
                throw new DuplicateHouseholdException("An owner named " + request.getLastName()
                        + " already exists at " + request.getAddress());
            }
        }
    }

    /**
     * Normalize a name or address for household comparison: trim, collapse any run of
     * whitespace to a single space, and lower-case. Yields {@code ""} for a {@code null} input.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
