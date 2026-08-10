package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that would put two owners with the same last name at the same
 * address. Last name and address are compared case-insensitively with collapsed whitespace (leading
 * and trailing whitespace trimmed, internal runs of whitespace treated as a single space), so
 * "Smith" / "12  Main  St " collides with "smith" / "12 Main St". A collision is reported as a 409
 * (see {@link DuplicateHouseholdException}).
 *
 * <p>Runs after {@link CheckUniqueTelephone} (which republishes the validated request as a variable)
 * and before {@link BuildOwner}. A request that opts in with {@code sharesHousehold=true} is allowed
 * through, so intentionally shared households can be recorded.
 */
public class CheckUniqueHousehold {

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

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
