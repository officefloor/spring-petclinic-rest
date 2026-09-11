package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>}:
 * CITY3 is the upper-cased first three letters of city, LAST3 the upper-cased first
 * three letters of lastName and NNNN a per-city 4-digit zero-padded sequence equal to
 * one more than the number of owners already in that city (e.g. {@code LON-SMI-0007}).
 * Runs after {@link BuildOwner} and before {@link SaveOwner}, so the sequence counts
 * existing owners in the city but not the one being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String lastName = owner.getLastName();
        String cityPrefix = prefix(city);
        String lastPrefix = prefix(lastName);
        long sequence = 1L;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsIgnoreCase(city, existing.getCity())) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastPrefix, sequence));
    }

    private static String prefix(String value) {
        String source = (value == null ? "" : value);
        return source.substring(0, Math.min(3, source.length())).toUpperCase();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
