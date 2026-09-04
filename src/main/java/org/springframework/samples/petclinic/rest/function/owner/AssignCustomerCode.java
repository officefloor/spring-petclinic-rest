package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where
 * CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first
 * three letters of the last name and NNNN is a per-city 4-digit zero-padded sequence equal
 * to one more than the number of owners already in that city (e.g. {@code LON-SMI-0007}).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = countOwnersInCity(owner, ownerRepository) + 1;
        String city3 = first3Upper(owner.getCity());
        String last3 = first3Upper(owner.getLastName());
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static int countOwnersInCity(Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getCity()).equals(city)) {
                count++;
            }
        }
        return count;
    }

    private static String first3Upper(String value) {
        String v = value == null ? "" : value;
        return v.substring(0, Math.min(3, v.length())).toUpperCase();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
