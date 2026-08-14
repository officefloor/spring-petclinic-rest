package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}.
 *
 * <p>The code is formatted {@code '<LAST3>-<NNNN>'}, where LAST3 is the upper-cased first
 * three letters of the owner's last name and NNNN is a global 4-digit zero-padded sequence
 * equal to one more than the current number of owners (e.g. {@code 'SMI-0007'}). Runs before
 * {@link SaveOwner}, so the count excludes the owner being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
