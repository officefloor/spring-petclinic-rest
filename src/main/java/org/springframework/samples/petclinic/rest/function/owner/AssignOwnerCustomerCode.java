package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code customerCode}. The
 * sequence number is one more than the owners already in the new owner's city (see
 * {@link SameCity}), so it is computed on the built owner before {@link SaveOwner} persists
 * it (the owner is not yet counted). The code is persisted with the row and returned on
 * later reads.
 *
 * @see CustomerCode for the {@code <CITY3>-<LAST3>-<NNNN>} format.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int inCity = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (SameCity.matches(existing, owner.getCity())) {
                inCity++;
            }
        }
        int sequence = inCity + 1;
        owner.setCustomerCode(CustomerCode.format(owner.getCity(), owner.getLastName(), sequence));
    }
}
