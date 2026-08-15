package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}, formatted
 * {@code '<LAST3>-<NNNN>'} where {@code LAST3} is the upper-cased first three letters of lastName
 * and {@code NNNN} is a global 4-digit zero-padded sequence equal to one more than the current
 * number of owners (e.g. {@code 'SMI-0007'}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}; {@code @Val} yields the built
 * owner so the code is mutated in place and persisted by the save step. The sequence is counted
 * before this owner is saved, so successive creates receive consecutive numbers.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", prefix, sequence));
    }
}
