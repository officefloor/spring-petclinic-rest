package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link BuildOwner} and before {@link SaveOwner} on {@code POST /api/owners}. Assigns the
 * new owner's {@code customerCode} formatted {@code <CITY3>-<LAST3>-<NNNN>}, where CITY3 is the
 * upper-cased first three letters of the owner's city, LAST3 the upper-cased first three letters of the
 * owner's lastName and NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of
 * owners already in that city (e.g. {@code LON-SMI-0007}). The owner is still unsaved, so the count
 * excludes it and the per-city sequence advances by one per create.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        String city3 = city.substring(0, Math.min(3, city.length())).toUpperCase();
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        long sequence = ownerRepository.findAll().stream()
            .filter(existing -> city.equals(existing.getCity()))
            .count() + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }
}
