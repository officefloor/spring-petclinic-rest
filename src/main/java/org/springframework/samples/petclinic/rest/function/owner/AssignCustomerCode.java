package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's customer code before it is saved. The sequence is per-city: one
 * more than the number of owners already in this owner's city (see {@link Cities#matches}).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = countInCity(owner, ownerRepository) + 1;
        owner.setCustomerCode(CustomerCode.format(owner.getCity(), owner.getLastName(), sequence));
    }

    private static int countInCity(Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (Cities.matches(existing, owner.getCity())) {
                count++;
            }
        }
        return count;
    }
}
