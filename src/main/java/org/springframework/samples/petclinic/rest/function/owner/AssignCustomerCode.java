package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<LAST3>-<NNNN>'} where
 * {@code LAST3} is the upper-cased first three letters of {@code lastName} and
 * {@code NNNN} is a global 4-digit zero-padded sequence equal to one more than the
 * current number of owners (e.g. {@code 'SMI-0007'}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the count it reads excludes the owner being
 * created; the code is then persisted with the new owner and returned by later reads.
 * It mutates the built {@link Owner} in place (see {@code @Val} semantics).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3(owner.getLastName()), sequence));
    }

    private static String last3(String lastName) {
        String prefix = lastName == null ? "" : lastName;
        return prefix.substring(0, Math.min(3, prefix.length())).toUpperCase();
    }
}
