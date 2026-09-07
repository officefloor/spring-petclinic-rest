package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for create-owner, now keyed on the household. The household is keyed
 * on {@code (lastName, postcode)} via the deterministic {@link Household#id(Owner) householdId}, so
 * a second owner sharing a last name and postcode is the same household: it is rejected with a 409
 * Conflict UNLESS the request opted in with {@code sharesHousehold} true, in which case it is
 * created as a declared household member. Runs after {@link AssignOwnerHousehold} has stamped the
 * householdId.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // opted in as a declared household member: bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new one
            }
            if (householdId != null && householdId.equals(Household.id(existing))) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
