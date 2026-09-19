package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request that would share a household (same last name and same
 * address) with an existing owner, responding 409. Last name and address are compared
 * case-insensitively with collapsed whitespace. A request may opt in to sharing a
 * household by setting {@code sharesHousehold} true, in which case the check is skipped.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        boolean clash = ownerRepository.findByLastName(request.getLastName()).stream()
                .anyMatch(owner -> normalize(owner.getLastName()).equals(lastName)
                        && normalize(owner.getAddress()).equals(address));
        if (clash) {
            throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
