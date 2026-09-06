package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects creating an owner when another owner already has the same last name and the same
 * address. Both are compared case-insensitively with runs of whitespace collapsed to a single
 * space. The check is skipped when the request set {@code sharesHousehold} true, which permits
 * multiple owners in one household.
 */
public class EnsureUniqueHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        boolean inHousehold = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .anyMatch(existing -> normalize(existing.getLastName()).equals(lastName)
                        && normalize(existing.getAddress()).equals(address));
        if (inHousehold) {
            throw new DuplicateHouseholdException(
                    "An owner named " + owner.getLastName() + " already exists at " + owner.getAddress());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
