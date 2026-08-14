package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}.
 *
 * <p>The code is formatted {@code '<CITY3>-<LAST3>-<NNNN>'}, where CITY3 is the upper-cased
 * first three letters of the owner's city, LAST3 the upper-cased first three letters of the
 * owner's last name and NNNN a per-city 4-digit zero-padded sequence equal to one more than
 * the number of owners already in that city (e.g. {@code 'LON-SMI-0007'}). Runs before
 * {@link SaveOwner}, so the count excludes the owner being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = (int) ownerRepository.findAll().stream()
                .filter(o -> city.equalsIgnoreCase(o.getCity()))
                .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
