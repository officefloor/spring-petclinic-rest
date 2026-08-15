package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

import net.officefloor.plugin.variable.Val;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has published the
 * request body. Rejects the request when another owner already shares the same household — the same
 * last name and the same address, compared case-insensitively with collapsed whitespace — so a
 * duplicate household is a 409 Conflict rather than a second owner in the same home.
 *
 * <p>A request may opt in with {@code sharesHousehold: true} to deliberately register another owner
 * at an existing household; in that case the check is skipped and the create proceeds.
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
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-case, trim and collapse internal whitespace runs to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
