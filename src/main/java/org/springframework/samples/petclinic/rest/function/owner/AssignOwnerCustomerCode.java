package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code customerCode} before it is saved: the 4-digit sequence is
 * one more than the number of owners already in the same city, so codes run consecutively
 * within each city.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long inCity = ownerRepository.findAll().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equals(owner.getCity()))
            .count();
        int sequence = (int) inCity + 1;
        owner.setCustomerCode(OwnerCustomerCodes.format(owner.getCity(), owner.getLastName(), sequence));
    }
}
