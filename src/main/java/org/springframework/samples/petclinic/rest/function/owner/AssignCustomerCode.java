package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} as {@code <CITY3>-<LAST3>-<NNNN>}: the upper-cased first
 * three letters of the city and last name, and a per-city 4-digit sequence one more than the owners
 * already in that city. Runs before the owner is saved, so the sequence excludes the owner being
 * created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = three(owner.getCity());
        String last3 = three(owner.getLastName());
        long inCity = ownerRepository.findAll().stream()
            .filter(o -> owner.getCity().equalsIgnoreCase(o.getCity())).count();
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, inCity + 1));
    }

    private static String three(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
