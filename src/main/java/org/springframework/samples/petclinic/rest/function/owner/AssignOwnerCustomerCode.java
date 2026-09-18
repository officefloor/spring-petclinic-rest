package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly built owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'}
 * where CITY3 is the upper-cased first three letters of the owner's city, LAST3 the upper-cased
 * first three letters of the owner's last name and NNNN is a per-city 4-digit zero-padded
 * sequence equal to one more than the number of owners already in that city (e.g. 'LON-SMI-0007').
 * The code is stored in place on the built entity so it is persisted and returned. Runs after
 * {@link BuildOwner} and before the owner is saved, within the same transaction as the count so
 * the sequence advances consistently.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long cityCount = ownerRepository.findAll().stream()
                .filter(existing -> owner.getCity().equalsIgnoreCase(existing.getCity()))
                .count();
        owner.setCustomerCode(customerCode(owner.getCity(), owner.getLastName(), cityCount));
    }

    private static String customerCode(String city, String lastName, long cityOwnerCount) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), cityOwnerCount + 1);
    }

    /** The upper-cased first three letters (fewer if the value is shorter) of the given value. */
    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
