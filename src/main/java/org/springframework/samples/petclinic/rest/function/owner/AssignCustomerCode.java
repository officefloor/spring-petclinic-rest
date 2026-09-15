package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted {@code <LAST3>-<NNNN>}
 * where LAST3 is the upper-cased first three letters of the owner's last name and NNNN is
 * a global 4-digit zero-padded sequence equal to one more than the current number of
 * owners (e.g. {@code SMI-0007}). Runs before {@link SaveOwner} and mutates the built
 * owner in place, so the code is stored and returned with the owner.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long existingOwners = ownerRepository.findAll(Pageable.ofSize(1)).getTotalElements();
        owner.setCustomerCode(customerCode(owner.getLastName(), existingOwners + 1));
    }

    private static String customerCode(String lastName, long sequence) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        return String.format("%s-%04d", last3, sequence);
    }
}
