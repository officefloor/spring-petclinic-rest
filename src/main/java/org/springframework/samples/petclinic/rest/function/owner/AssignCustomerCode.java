package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a customer code to a newly created owner, formatted
 * '&lt;UPPERCASE_CITY&gt;-&lt;NNNN&gt;' where NNNN is one more than the number of
 * owners already in that city, zero-padded to four digits (e.g. 'LONDON-0007').
 * The owner being created has not yet been saved, so it is not counted.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equals(existing.getCity()))
                .count();
        String prefix = city == null ? "" : city.toUpperCase();
        owner.setCustomerCode(String.format("%s-%04d", prefix, inCity + 1));
    }
}
