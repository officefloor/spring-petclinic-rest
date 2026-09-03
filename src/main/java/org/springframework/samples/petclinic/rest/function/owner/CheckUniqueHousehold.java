package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create whose computed {@code householdId} (derived from lastName and postcode)
 * already belongs to another owner, before {@link SaveOwner} runs. Handled with 409 by
 * {@code DuplicateHouseholdHandler}. Skipped when the request opts in with
 * {@code sharesHousehold=true}, so a declared household member is created instead.
 */
public class CheckUniqueHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String householdId = owner.getHouseholdId();
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId())
                    && householdId.equals(other.getHouseholdId())) {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getPostcode());
            }
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space so that
     *  case and spacing differences do not defeat the household match. */
    static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
