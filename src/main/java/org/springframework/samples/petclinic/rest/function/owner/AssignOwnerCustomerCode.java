package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * On create, assigns the owner's {@code customerCode} as '&lt;LAST3&gt;-&lt;NNNN&gt;', where LAST3 is the
 * upper-cased first three letters of the last name and NNNN is a global 4-digit zero-padded sequence
 * equal to one more than the current number of owners (e.g. 'SMI-0007'). Runs before {@code SaveOwner}
 * so the not-yet-persisted owner is excluded from the count.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", prefix, sequence));
    }
}
