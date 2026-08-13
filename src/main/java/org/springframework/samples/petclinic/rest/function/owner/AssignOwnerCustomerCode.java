package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is
 * the upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the
 * last name and NNNN a per-city 4-digit zero-padded sequence equal to one more than the owners already
 * in that city (e.g. {@code SYD-SMI-0007}). Runs after {@link BuildOwner} maps the request and before
 * {@link SaveOwner} persists it, so the count reflects the owners already stored, not the one being
 * created.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase(Locale.ROOT);
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(o -> city.equalsIgnoreCase(o.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
