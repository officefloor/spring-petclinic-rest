package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} to the freshly built {@link Owner} before it is saved.
 *
 * <p>The code is formatted {@code '<CITY3>-<LAST3>-<NNNN>'}: CITY3 is the upper-cased first
 * three letters of the owner's city, LAST3 the upper-cased first three letters of the owner's
 * last name, and NNNN a per-city 4-digit zero-padded sequence equal to one more than the owners
 * already in that city (e.g. {@code 'SPR-SMI-0007'}). Running before {@link SaveOwner} means the
 * new owner is not yet counted, so the sequence increments by one per created owner in that city.
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
