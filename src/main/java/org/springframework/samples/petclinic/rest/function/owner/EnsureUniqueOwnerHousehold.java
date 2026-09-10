package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose last name and address match an existing owner,
 * responding 409. Names and addresses are compared case-insensitively with collapsed
 * whitespace. A request that sets {@code sharesHousehold} true bypasses the check so
 * genuine housemates can be registered. Runs after {@link ValidateOwnerFields} (which
 * has already ensured last name and address are present) and before {@link BuildOwner}.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
