package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records on a newly built owner how many members its household has after this create —
 * the existing owners sharing its {@code householdId} plus the new owner itself. Runs
 * after {@link AssignHousehold} (so the shared id is set and back-filled) and before
 * {@link SaveOwner} (so the new owner is not yet persisted and thus counted only via the
 * {@code +1}), mutating the built owner in place so the size is stored and returned with
 * it. An owner with no shared household ({@code householdId} unset) has a size of one.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setHouseholdSize(Households.memberCount(ownerRepository, owner.getHouseholdId()) + 1);
    }
}
