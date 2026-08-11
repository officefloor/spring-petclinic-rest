package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}:
 * CITY3 is the upper-cased first three letters of the owner's city, LAST3 the upper-cased
 * first three letters of the owner's last name, and NNNN a per-city 4-digit zero-padded
 * sequence equal to one more than the number of owners already in that city (e.g.
 * {@code 'SYD-SMI-0007'}). Runs after {@link BuildOwner} (the entity, hence its city and
 * last name, exists) and before {@link SaveOwner} (so the new owner is not yet counted),
 * and mutates the built {@link Owner} in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        String city = normalize(owner.getCity());
        int sequence = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    /** Upper-cased first three letters of the value. */
    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

    /** Lower-case and trim, treating null as empty, for case-insensitive city comparison. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
