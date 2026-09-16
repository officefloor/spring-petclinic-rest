package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's customer code, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is
 * the upper-cased first three letters of the city, LAST3 the upper-cased first three letters
 * of the last name and NNNN a per-city 4-digit zero-padded sequence equal to one more than
 * the number of owners already in that city. Runs before the owner is saved and mutates the
 * built {@link Owner} in place.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String cityPrefix = prefix(owner.getCity());
        String lastNamePrefix = prefix(owner.getLastName());
        int sequence = OwnerCities.size(ownerRepository.findAll(), owner.getCity()) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastNamePrefix, sequence));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
