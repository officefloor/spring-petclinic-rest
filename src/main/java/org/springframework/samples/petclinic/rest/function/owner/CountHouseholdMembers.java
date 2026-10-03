package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners that belong to the same
 * household — the existing members selected by {@link Households#members} — plus one for
 * the owner being created. Runs after {@link AssignHousehold} has stamped the
 * {@code householdId}, so it reflects that same household definition, and before the owner
 * is saved, so the existing members are counted once and the new owner is added as the
 * {@code + 1}.
 *
 * <p>An owner with no postcode belongs to no household (null {@code householdId}), so it
 * has no existing members and is recorded as a household of one.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existingMembers = Households.members(owner.getHouseholdId(), ownerRepository.findAll()).size();
        owner.setHouseholdSize(existingMembers + 1);
    }
}
