package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdConflictException;

/**
 * Rejects a create request whose last name and address both match those of an existing
 * owner, comparing case-insensitively with collapsed whitespace. Runs after
 * {@link ValidateOwnerFields}, so it sees the validated request, and before
 * {@link BuildOwner}, so a duplicate is a 409 rather than a persisted record. A request
 * that sets {@code sharesHousehold} true is allowed through.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdConflictException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                throw new OwnerHouseholdConflictException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
