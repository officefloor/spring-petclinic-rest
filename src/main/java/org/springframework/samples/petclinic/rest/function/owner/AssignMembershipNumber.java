package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class AssignMembershipNumber {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int currentOwnerCount = ownerRepository.findAll().size();
        owner.setMembershipNumber(currentOwnerCount + 1);
    }
}
