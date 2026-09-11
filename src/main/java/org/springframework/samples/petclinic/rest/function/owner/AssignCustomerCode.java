package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}
 * where CITY3 is the upper-cased first three letters of city, LAST3 the upper-cased
 * first three letters of lastName, and NNNN a per-city 4-digit zero-padded sequence
 * equal to one more than the owners already in that city (e.g. {@code SYD-SMI-0007}).
 * Runs before the owner is saved, so the count reflects the owners that already exist.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        String city = owner.getCity();
        long sequence = ownerRepository.findAll().stream()
            .filter(existing -> city == null ? existing.getCity() == null : city.equals(existing.getCity()))
            .count() + 1L;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix(String value) {
        int end = Math.min(3, value.length());
        return value.substring(0, end).toUpperCase(Locale.ROOT);
    }
}
