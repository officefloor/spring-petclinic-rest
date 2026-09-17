package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code customerCode}. The
 * sequence number is one more than the current number of owners, so it is computed on the
 * built owner before {@link SaveOwner} persists it (the owner is not yet counted). The code
 * is persisted with the row and returned on later reads.
 *
 * @see CustomerCode for the {@code <LAST3>-<NNNN>} format.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(CustomerCode.format(owner.getLastName(), sequence));
    }
}
