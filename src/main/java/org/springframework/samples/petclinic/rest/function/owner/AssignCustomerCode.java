package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<LAST3>-<NNNN>'} where LAST3 is the
 * upper-cased first three letters of the last name and NNNN is a global 4-digit zero-padded sequence
 * equal to one more than the current number of owners (e.g. 'SMI-0007'). Runs after the owner is built
 * but before it is saved, so the count reflects the existing owners and the code is mutated in place via
 * {@code @Val} for the save/respond steps to persist and return.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        int len = Math.min(3, lastName.length());
        String last3 = lastName.substring(0, len).toUpperCase(Locale.ROOT);
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
