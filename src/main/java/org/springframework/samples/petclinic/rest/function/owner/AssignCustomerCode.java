package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}
 * where CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased
 * first three of the last name, and NNNN a per-city 4-digit zero-padded sequence equal
 * to one more than the number of owners already in that city. Runs before {@code save},
 * so the new owner is not yet counted.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
