package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a sequential membership number equal to one more
 * than the current number of owners, so the first owner created after {@code n}
 * existing owners receives membership number {@code n + 1}.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setMembershipNumber(ownerRepository.findAll().size() + 1);
    }
}
