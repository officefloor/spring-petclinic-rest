package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code customerCode} to a newly built owner, formatted
 * {@code <CITY3>-<LAST3>-<NNNN>} where {@code CITY3} is the upper-cased first three
 * letters of the city, {@code LAST3} is the upper-cased first three letters of the last
 * name and {@code NNNN} is a per-city 4-digit zero-padded sequence equal to one more than
 * the number of owners already in that city (e.g. {@code SYD-SMI-0007}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}, so the owner being
 * created is not yet counted: {@code NNNN} = existing owners in the city + 1. Mutates the
 * owner in place (the same object {@link SaveOwner} persists).
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity() == null ? "" : owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(o -> city.equals(o.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
