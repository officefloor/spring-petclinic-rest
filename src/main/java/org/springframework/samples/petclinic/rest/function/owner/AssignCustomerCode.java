package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in the create-owner pipeline before {@link SaveOwner}. Assigns the owner's
 * {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is the upper-cased
 * first three letters of the city, LAST3 the upper-cased first three letters of the last name and
 * NNNN is a per-city 4-digit zero-padded sequence equal to one more than the number of owners
 * already in that city (e.g. 'SYD-SMI-0007'). Mutates the built owner in place so later steps store
 * and respond with the assigned code.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
