package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's customer code before it is saved. The code is formatted
 * {@code <CITY3>-<LAST3>-<NNNN>}, where {@code CITY3} is the upper-cased first three
 * letters of the city, {@code LAST3} the upper-cased first three letters of the last name
 * and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more than the
 * number of owners already in that city (e.g. {@code SYD-SMI-0007}). Runs after
 * {@link BuildOwner} has produced the entity and before {@link SaveOwner} persists it, so
 * {@code findAll()} sees only the owners that existed before this create; mutates the
 * built owner in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        int sequence = countInCity(ownerRepository, city) + 1;
        owner.setCustomerCode(format(city, owner.getLastName(), sequence));
    }

    private static int countInCity(OwnerRepository ownerRepository, String city) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }

    private static String format(String city, String lastName, int sequence) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), sequence);
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
