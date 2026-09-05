package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} formatted {@code <LAST3>-<NNNN>}, where LAST3 is the
 * upper-cased first three letters of the owner's last name and NNNN is a global 4-digit
 * zero-padded sequence equal to one more than the current number of owners.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String last3 = lastName.length() >= 3 ? lastName.substring(0, 3) : lastName;
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3.toUpperCase(), sequence));
    }
}
