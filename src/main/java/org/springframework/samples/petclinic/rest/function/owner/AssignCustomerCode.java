package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} as {@code <CITY3>-<LAST3>-<NNNN>}: the
 * upper-cased first three letters of the city and of the last name, plus a per-city
 * 4-digit sequence one greater than the owners already in that city.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long inCity = ownerRepository.findAll().stream()
            .filter(o -> owner.getCity().equalsIgnoreCase(o.getCity()))
            .count();
        String city = prefix(owner.getCity());
        String last = prefix(owner.getLastName());
        owner.setCustomerCode(String.format("%s-%s-%04d", city, last, inCity + 1));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
