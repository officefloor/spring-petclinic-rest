package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} formatted {@code <CITY3>-<LAST3>-<NNNN>}, where CITY3 is
 * the upper-cased first three letters of city, LAST3 the upper-cased first three of lastName and
 * NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of owners already
 * in that city (e.g. {@code TOW-SMI-0007}). Runs after the owner is built and before it is saved,
 * so the count reflects existing owners only.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String cityPrefix = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastName = owner.getLastName();
        String lastPrefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(o -> city.equalsIgnoreCase(o.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence));
    }
}
