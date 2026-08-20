package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request whose last name and address both match an existing owner's,
 * throwing {@link DuplicateHouseholdException} for a 409. The address is compared in its normalized
 * form (see {@link AddressNormalizer}); the last name is compared case-insensitively with runs of
 * whitespace collapsed to a single space (and leading/trailing whitespace trimmed). The check is
 * skipped when the request opts in via {@code sharesHousehold}, acknowledging a deliberately shared
 * household.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalizeName(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalizeName(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
