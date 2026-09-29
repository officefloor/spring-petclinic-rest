package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}
 * where CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased
 * first three letters of the last name and NNNN is a per-city 4-digit zero-padded
 * sequence equal to one more than the number of owners already in that city (e.g.
 * {@code SYD-SMI-0007}). Runs before the owner is saved, so the count excludes the owner
 * being created and successive creates in the same city receive consecutive sequence
 * numbers.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = first3(city);
        String last3 = first3(owner.getLastName());
        int sequence = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    /** Upper-cased first three letters of {@code value} (fewer if it is shorter). */
    private static String first3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
