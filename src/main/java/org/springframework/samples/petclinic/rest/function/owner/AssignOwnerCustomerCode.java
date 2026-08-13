package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>} where LAST3 is the
 * upper-cased first three letters of the last name and NNNN is a global 4-digit zero-padded sequence
 * equal to one more than the current number of owners (e.g. {@code SMI-0007}). Runs after
 * {@link BuildOwner} maps the request and before {@link SaveOwner} persists it, so the count reflects
 * the owners already stored, not the one being created.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
