package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create whose lastName and address already belong to another owner (compared
 * case-insensitively with collapsed whitespace), before {@link SaveOwner} runs. Handled
 * with 409 by {@code DuplicateHouseholdHandler}. Skipped when the request opts in with
 * {@code sharesHousehold=true}.
 */
public class CheckUniqueHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId())
                    && lastName.equals(normalize(other.getLastName()))
                    && address.equals(normalize(other.getAddress()))) {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space so that
     *  case and spacing differences do not defeat the household match. */
    static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
