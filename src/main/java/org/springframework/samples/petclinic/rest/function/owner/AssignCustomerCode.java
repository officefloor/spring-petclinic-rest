package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} formatted {@code <CITY3>-<LAST3>-<NNNN>}, where CITY3 is the
 * upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the last
 * name, and NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of owners
 * already in that city.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = prefix(city);
        String last3 = prefix(owner.getLastName());
        int sequence = countInCity(ownerRepository, city) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    /** Upper-cased first three letters of {@code value}. */
    private static String prefix(String value) {
        String s = value == null ? "" : value;
        return s.substring(0, Math.min(3, s.length())).toUpperCase();
    }

    /** Number of existing owners in the same city, compared case-insensitively. */
    private static int countInCity(OwnerRepository ownerRepository, String city) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }
}
