package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is
 * the upper-cased first three letters of city, LAST3 the upper-cased first three letters of lastName
 * and NNNN is a per-city 4-digit zero-padded sequence equal to one more than the owners already in
 * that city (e.g. {@code 'SYD-SMI-0007'}).
 *
 * <p>Runs after {@link BuildOwner} has mapped the request onto the entity and before {@link SaveOwner},
 * within the same write transaction; it counts the same-city owners persisted so far (excluding this
 * new, not-yet-saved one) so each created owner gets the next sequence number for its city.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = 1;
        for (Owner other : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(other.getCity())) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
