package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly built owner's {@code customerCode}, formatted {@code '<LAST3>-<NNNN>'}
 * where LAST3 is the upper-cased first three letters of the owner's last name and NNNN is a
 * global 4-digit zero-padded sequence equal to one more than the current number of owners
 * (e.g. 'SMI-0007'). The code is stored in place on the built entity so it is persisted and
 * returned. Runs after {@link BuildOwner} and before the owner is saved, within the same
 * transaction as the count so the sequence advances consistently.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setCustomerCode(customerCode(owner.getLastName(), ownerRepository.findAll().size()));
    }

    private static String customerCode(String lastName, int currentOwnerCount) {
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        return String.format("%s-%04d", prefix, currentOwnerCount + 1);
    }
}
