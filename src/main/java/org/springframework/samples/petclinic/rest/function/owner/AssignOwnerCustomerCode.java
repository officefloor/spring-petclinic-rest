package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where
 * {@code CITY3} is the upper-cased first three letters of the city, {@code LAST3} the
 * upper-cased first three letters of the last name, and {@code NNNN} a per-city 4-digit
 * zero-padded sequence equal to one more than the number of owners already in that city
 * (e.g. {@code SYD-SMI-0007}).
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String cityPrefix = prefix(owner.getCity());
        String lastPrefix = prefix(owner.getLastName());
        int sequence = 1 + (int) ownerRepository.findAll().stream()
                .filter(existing -> owner.getCity() != null
                        && owner.getCity().equalsIgnoreCase(existing.getCity()))
                .count();
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence));
    }

    private static String prefix(String value) {
        String s = value == null ? "" : value;
        return s.substring(0, Math.min(3, s.length())).toUpperCase(Locale.ROOT);
    }
}
