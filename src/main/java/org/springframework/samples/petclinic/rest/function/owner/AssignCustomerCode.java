package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where
 * CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first three
 * letters of the last name and NNNN is a 4-digit zero-padded per-city sequence equal to one
 * more than the number of owners already in that city (e.g. {@code SYD-SMI-0007}).
 *
 * <p>Runs before {@link SaveOwner}, so the new owner is not yet persisted and the count
 * reflects only the existing owners.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        long sequence = inCity + 1;
        String city3 = prefix(city);
        String last3 = prefix(owner.getLastName());
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix(String value) {
        int len = Math.min(3, value.length());
        return value.substring(0, len).toUpperCase();
    }
}
