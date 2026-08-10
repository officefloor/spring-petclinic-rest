package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is the
 * upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the last name
 * and NNNN a per-city 4-digit zero-padded sequence equal to one more than the owners already in that city
 * (e.g. 'LON-SMI-0007'). Runs after the owner is built but before it is saved, so the count reflects the
 * existing owners and the code is mutated in place via {@code @Val} for the save/respond steps to persist
 * and return.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix3(owner.getCity());
        String last3 = prefix3(owner.getLastName());
        int sequence = (int) ownerRepository.findAll().stream()
            .filter(existing -> existing.getCity() != null
                && existing.getCity().equalsIgnoreCase(owner.getCity()))
            .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix3(String value) {
        int len = Math.min(3, value.length());
        return value.substring(0, len).toUpperCase(Locale.ROOT);
    }
}
