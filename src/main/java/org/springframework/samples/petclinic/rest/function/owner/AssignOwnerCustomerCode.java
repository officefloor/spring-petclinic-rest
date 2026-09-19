package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the new owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>}: the
 * upper-cased first three letters of the last name, a hyphen, and a global 4-digit
 * zero-padded sequence equal to one more than the current number of owners (e.g.
 * {@code SMI-0007}). Runs after {@link BuildOwner} (so the entity and its last name
 * exist) and before {@link SaveOwner} persists the code.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(format(owner.getLastName(), sequence));
    }

    private static String format(String lastName, int sequence) {
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        return String.format("%s-%04d", prefix, sequence);
    }
}
