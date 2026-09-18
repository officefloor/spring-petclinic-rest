package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted
 * {@code <LAST3>-<NNNN>}: LAST3 is the upper-cased first three letters of the last
 * name and NNNN is a global 4-digit zero-padded sequence equal to one more than the
 * current number of owners (e.g. {@code SMI-0007}).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String last3 = owner.getLastName().substring(0, Math.min(3, owner.getLastName().length()))
            .toUpperCase(Locale.ROOT);
        long sequence = ownerRepository.count() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
