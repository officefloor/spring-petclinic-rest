package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a {@code customerCode} formatted
 * {@code '<LAST3>-<NNNN>'}: LAST3 is the upper-cased first three letters of the
 * owner's last name and NNNN is a global 4-digit zero-padded sequence equal to
 * one more than the current number of owners. Runs before {@link SaveOwner} so
 * the owner being created is not yet counted. Mutates the {@link Owner} in place
 * so it is persisted and returned with the code.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        int len = Math.min(3, lastName.length());
        String last3 = lastName.substring(0, len).toUpperCase(Locale.ROOT);
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
