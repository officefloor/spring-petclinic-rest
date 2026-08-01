package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a customer code formatted '<UPPERCASE_CITY>-<NNNN>',
 * where NNNN is one more than the number of owners already in that city, zero-padded
 * to four digits (e.g. 'LONDON-0007').
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city.equals(existing.getCity()))
                .count();
        owner.setCustomerCode(String.format("%s-%04d", city.toUpperCase(), inCity + 1));
    }
}
