package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects 409 when another owner already has the same last name and address as the built owner,
 * compared case-insensitively with collapsed whitespace. A request that opts in with
 * {@code sharesHousehold: true} is allowed through, since the owners deliberately share a household.
 */
public class RejectDuplicateOwnerHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(owner.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && lastName.equals(normalize(other.getLastName()))
                    && address.equals(normalize(other.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
