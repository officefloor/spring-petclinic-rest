package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} formatted {@code <CITY3>-<LAST3>-<NNNN>}, where CITY3 is
 * the upper-cased first three letters of the owner's city, LAST3 is the upper-cased first
 * three letters of the owner's last name and NNNN is a per-city 4-digit zero-padded sequence
 * equal to one more than the number of owners already in that city.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity() == null ? "" : owner.getCity();
        String city3 = city.length() >= 3 ? city.substring(0, 3) : city;
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String last3 = lastName.length() >= 3 ? lastName.substring(0, 3) : lastName;
        int sequence = (int) ownerRepository.findAll().stream()
            .filter(o -> o.getCity() != null && o.getCity().equalsIgnoreCase(city))
            .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3.toUpperCase(), last3.toUpperCase(), sequence));
    }
}
