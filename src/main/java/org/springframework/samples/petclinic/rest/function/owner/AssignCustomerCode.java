package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is
 * the upper-cased first three letters of city, LAST3 the upper-cased first three letters of lastName
 * and NNNN a per-city 4-digit zero-padded sequence equal to one more than the owners already in that
 * city (e.g. {@code SYD-SMI-0007}). Runs before {@link SaveOwner} so the owner being created is not
 * itself counted.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String cityPrefix = prefix(city);
        String lastPrefix = prefix(owner.getLastName());
        long sequence = ownerRepository.findAll().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
