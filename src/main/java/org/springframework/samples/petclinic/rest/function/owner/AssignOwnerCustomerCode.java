package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} as {@code <CITY3>-<LAST3>-<NNNN>}: the
 * upper-cased first three letters of the city and last name, plus a per-city 4-digit
 * sequence equal to one more than the owners already in that city.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> owner.getCity().equals(existing.getCity())).count();
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, inCity + 1));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
