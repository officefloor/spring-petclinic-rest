package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} to a newly built owner, formatted
 * {@code <LAST3>-<NNNN>} where {@code LAST3} is the upper-cased first three letters of
 * the last name and {@code NNNN} is a global 4-digit zero-padded sequence equal to one
 * more than the current number of owners (e.g. {@code SMI-0007}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}, so the owner being
 * created is not yet counted: {@code NNNN} = existing owner count + 1. Mutates the owner
 * in place (the same object {@link SaveOwner} persists).
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
