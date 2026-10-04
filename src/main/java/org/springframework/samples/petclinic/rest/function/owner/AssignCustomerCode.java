package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>} where
 * {@code LAST3} is the upper-cased first three letters of {@code lastName} and
 * {@code NNNN} is a global 4-digit zero-padded sequence equal to one more than the
 * current number of owners (e.g. {@code SMI-0007}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}, so the count reflects
 * the owners that already exist and excludes the owner being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        int length = Math.min(3, lastName.length());
        String prefix = lastName.substring(0, length).toUpperCase();
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", prefix, sequence));
    }
}
