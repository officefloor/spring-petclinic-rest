package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners share the built owner's household — the owners with the same last
 * name and postcode (see {@link OwnerHouseholds}) — counting this owner as well. Runs after
 * {@link AssignOwnerHousehold} has stamped the id but before the owner is saved, so the count
 * reflects the household size after this create, and mutates the built {@link Owner} in place.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int size = 1 + OwnerHouseholds.members(Owners.active(ownerRepository.findAll()),
                owner.getLastName(), owner.getPostcode()).size();
        owner.setHouseholdSize(size);
    }
}
