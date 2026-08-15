package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects a create-owner request whose last name and address both match an existing owner,
 * compared case-insensitively with collapsed whitespace (a shared household). Runs after
 * {@link ValidateNewOwner} has published the request, and before {@link BuildOwner}, so a
 * duplicate is a 409 rather than a persisted record. The request opts out by setting
 * {@code sharesHousehold} true, in which case the check is skipped.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner owner : ownerRepository.findAll()) {
            if (lastName.equals(normalize(owner.getLastName()))
                    && address.equals(normalize(owner.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-case and collapse all runs of whitespace to a single space, trimmed. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
