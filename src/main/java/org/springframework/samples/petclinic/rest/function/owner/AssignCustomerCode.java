package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} formatted {@code <CITY3>-<LAST3>-<NNNN>}: the
 * upper-cased first three letters of the city and of the last name, plus a per-city
 * 4-digit sequence equal to one more than the owners already in that city.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix3(owner.getCity());
        String last3 = prefix3(owner.getLastName());
        int sequence = countInCity(owner.getCity(), ownerRepository) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

    private static int countInCity(String city, OwnerRepository ownerRepository) {
        return (int) ownerRepository.findAll().stream()
                .filter(o -> city.equalsIgnoreCase(o.getCity()))
                .count();
    }
}
