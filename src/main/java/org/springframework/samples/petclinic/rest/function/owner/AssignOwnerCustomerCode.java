package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} formatted {@code <CITY3>-<LAST3>-<NNNN>}: CITY3 and
 * LAST3 are the upper-cased first three letters of the city and last name, and NNNN is a per-city
 * 4-digit zero-padded sequence equal to one more than the owners already in that city. Runs before
 * {@link SaveOwner}, so this owner is not yet counted (e.g. 'LON-SMI-0007').
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        int sequence = cityCount(owner.getCity(), ownerRepository) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }

    private static int cityCount(String city, OwnerRepository ownerRepository) {
        return (int) ownerRepository.findAll().stream()
            .filter(existing -> city.equals(existing.getCity()))
            .count();
    }
}
