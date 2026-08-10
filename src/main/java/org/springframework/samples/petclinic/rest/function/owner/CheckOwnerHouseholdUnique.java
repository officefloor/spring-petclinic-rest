package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdConflictException;

/**
 * Runs after {@link CheckOwnerTelephoneUnique} and before {@link BuildOwner}: rejects the request
 * with 409 when another owner already has the same last name at the same address. Both the last
 * name and the address are compared case-insensitively with runs of whitespace collapsed to a
 * single space, so differently-cased or -spaced duplicates are still caught. A request may opt out
 * of the check by setting {@code sharesHousehold: true} (two owners sharing one household).
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdConflictException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                throw new OwnerHouseholdConflictException("An owner with last name "
                        + request.getLastName() + " already lives at " + request.getAddress());
            }
        }
    }

    /** Lower-case, trim, and collapse internal whitespace runs to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
