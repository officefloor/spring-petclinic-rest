package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}
 * where CITY3 is the upper-cased first three letters of the city, LAST3 is the
 * upper-cased first three letters of the last name and NNNN is a per-city 4-digit
 * zero-padded sequence equal to one more than the number of owners already in that
 * city (e.g. {@code LON-SMI-0007}).
 *
 * <p>Runs after {@code BuildOwner} (so the owner exists) but before {@code SaveOwner}
 * (so the owner being created is not yet counted), mutating the owner in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long sequence = ownerRepository.findAll().stream()
                .filter(o -> city == null ? o.getCity() == null : city.equals(o.getCity()))
                .count() + 1;
        String city3 = prefix3(city);
        String last3 = prefix3(owner.getLastName());
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix3(String value) {
        String v = value == null ? "" : value;
        return v.substring(0, Math.min(3, v.length())).toUpperCase();
    }
}
