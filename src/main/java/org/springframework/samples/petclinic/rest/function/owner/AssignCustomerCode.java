package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's customerCode, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is the
 * upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the last
 * name and NNNN a per-city 4-digit zero-padded sequence equal to one more than the owners already in
 * that city (e.g. {@code SYD-SMI-0007}). Runs before {@link SaveOwner}, so the count excludes the
 * owner currently being created. Mutates the built {@link Owner} in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long sequence = ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", prefix(city), prefix(owner.getLastName()), sequence));
    }

    /** The upper-cased first three letters of the value (or fewer when it is shorter). */
    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
