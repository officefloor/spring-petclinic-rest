package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted
 * {@code <CITY3>-<LAST3>-<NNNN>}: CITY3 is the upper-cased first three letters of the
 * city, LAST3 the upper-cased first three letters of the last name, and NNNN a per-city
 * 4-digit zero-padded sequence equal to one more than the number of owners already in
 * that city (e.g. {@code LON-SMI-0007}).
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        long sequence = ownerRepository.countByCity(owner.getCity()) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    /** Upper-cased first three letters of {@code value} (fewer if it is shorter). */
    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
