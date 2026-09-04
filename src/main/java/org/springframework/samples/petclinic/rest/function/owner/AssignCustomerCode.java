package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} as {@code <LAST3>-<NNNN>}: the upper-cased
 * first three letters of the last name and a global 4-digit sequence one greater than
 * the current number of owners.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        String lastName = owner.getLastName();
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        owner.setCustomerCode(String.format("%s-%04d", prefix, sequence));
    }
}
