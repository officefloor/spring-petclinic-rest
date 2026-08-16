package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where
 * CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first three
 * letters of the last name, and NNNN a per-city 4-digit zero-padded sequence equal to one more
 * than the number of owners already in that city (e.g. {@code SYD-SMI-0007}).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String cityPrefix = prefix(owner.getCity());
        String lastNamePrefix = prefix(owner.getLastName());
        String city = normalize(owner.getCity());
        int sequence = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastNamePrefix, sequence));
    }

    /** Upper-cased first three letters of {@code value}. */
    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }

    /** Lower-cases and trims so the city comparison ignores case and surrounding whitespace. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
