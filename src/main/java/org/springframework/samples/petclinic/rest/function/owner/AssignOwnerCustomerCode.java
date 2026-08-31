package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * On create, assigns the owner's {@code customerCode} as '&lt;CITY3&gt;-&lt;LAST3&gt;-&lt;NNNN&gt;', where
 * CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first three of the last
 * name and NNNN a per-city 4-digit zero-padded sequence equal to one more than the owners already in that
 * city (e.g. 'LON-SMI-0007'). Runs before {@code SaveOwner} so the not-yet-persisted owner is excluded
 * from the count.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity() == null ? "" : owner.getCity();
        int sequence = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(existing.getCity())) {
                sequence++;
            }
        }
        owner.setCustomerCode(String.format("%s-%s-%04d", first3(city), first3(owner.getLastName()), sequence));
    }

    private static String first3(String value) {
        String text = value == null ? "" : value;
        return text.substring(0, Math.min(3, text.length())).toUpperCase();
    }
}
