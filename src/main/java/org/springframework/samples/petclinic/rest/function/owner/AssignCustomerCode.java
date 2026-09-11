package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>} where
 * LAST3 is the upper-cased first three letters of lastName and NNNN is a global
 * 4-digit zero-padded sequence equal to one more than the current number of owners
 * (e.g. {@code SMI-0007}). Runs before the owner is saved, so the count reflects the
 * owners that already exist.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        int end = Math.min(3, lastName.length());
        String last3 = lastName.substring(0, end).toUpperCase(Locale.ROOT);
        long sequence = ownerRepository.findAll().size() + 1L;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
