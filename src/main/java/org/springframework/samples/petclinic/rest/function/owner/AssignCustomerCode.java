package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where
 * CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first three
 * letters of the last name, and NNNN a per-city 4-digit zero-padded sequence equal to one more
 * than the number of owners already in that city (e.g. 'SYD-SMI-0007'). Runs after
 * {@link BuildOwner} and before {@link SaveOwner}, so it mutates the not-yet-persisted owner in
 * place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = prefix3(city);
        String last3 = prefix3(owner.getLastName());
        int sequence = countInCity(owner, city, ownerRepository) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

    private static int countInCity(Owner owner, String city, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }
}
