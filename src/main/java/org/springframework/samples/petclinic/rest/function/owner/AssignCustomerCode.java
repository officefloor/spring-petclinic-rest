package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is
 * the upper-cased first three letters of city, LAST3 the upper-cased first three letters of lastName,
 * and NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of owners
 * already in that city (e.g. {@code SYD-SMI-0007}). Runs before the owner is saved, so the count
 * excludes the owner being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        int sequence = countOwnersInCity(owner.getCity(), ownerRepository) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix(String value) {
        int length = Math.min(3, value.length());
        return value.substring(0, length).toUpperCase();
    }

    private static int countOwnersInCity(String city, OwnerRepository ownerRepository) {
        return (int) ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
    }
}
