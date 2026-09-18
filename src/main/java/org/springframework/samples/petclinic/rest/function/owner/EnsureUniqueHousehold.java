package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that would add a second owner to a household already occupied by an
 * existing owner — one sharing the same last name and address, compared case-insensitively and with
 * collapsed whitespace. A request may opt in to sharing a household by setting {@code sharesHousehold}
 * true, which skips the check. Rejects with 409 otherwise.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = canonical(request.getLastName());
        String address = canonical(request.getAddress());
        for (Owner existing : ownerRepository.findByLastName(request.getLastName())) {
            if (canonical(existing.getLastName()).equals(lastName)
                    && canonical(existing.getAddress()).equals(address)) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Case-insensitive form with leading/trailing and repeated inner whitespace collapsed to one space. */
    private static String canonical(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
