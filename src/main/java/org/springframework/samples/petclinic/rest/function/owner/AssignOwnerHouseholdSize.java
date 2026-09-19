package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code householdSize}: the number of owners in its household
 * (same last name and address, see {@link Households}) once this owner is added — the
 * existing household members plus the new owner itself. Runs after {@link BuildOwner} (so
 * the entity, its name and normalized address exist) and before {@link SaveOwner} persists
 * the count, so the freshly created owner is counted exactly once.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existing = Households.membersOf(ownerRepository, owner.getLastName(), owner.getAddress()).size();
        owner.setHouseholdSize(existing + 1);
    }
}
