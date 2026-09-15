package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted
 * {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is the upper-cased first three letters of
 * the owner's city, LAST3 the upper-cased first three letters of its last name and NNNN
 * a per-city 4-digit zero-padded sequence equal to one more than the owners already in
 * that city (e.g. {@code LON-SMI-0007}). Runs before {@link SaveOwner} and mutates the
 * built owner in place, so the code is stored and returned with the owner.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long ownersInCity = Cities.countIn(ownerRepository, owner.getCity());
        owner.setCustomerCode(customerCode(owner.getCity(), owner.getLastName(), ownersInCity + 1));
    }

    private static String customerCode(String city, String lastName, long sequence) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), sequence);
    }

    /** Upper-cased first three letters of the value, used as a code segment. */
    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
