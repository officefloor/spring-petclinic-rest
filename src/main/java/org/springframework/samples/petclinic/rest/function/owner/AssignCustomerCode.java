package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>}: LAST3 is
 * the upper-cased first three letters of lastName and NNNN is a global 4-digit
 * zero-padded sequence equal to one more than the current number of owners (e.g.
 * {@code SMI-0007}). Runs after {@link BuildOwner} and before {@link SaveOwner}, so the
 * sequence counts existing owners but not the one being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String prefix = (lastName == null ? "" : lastName);
        prefix = prefix.substring(0, Math.min(3, prefix.length())).toUpperCase();
        long sequence = ownerRepository.findAll().size() + 1L;
        owner.setCustomerCode(String.format("%s-%04d", prefix, sequence));
    }
}
