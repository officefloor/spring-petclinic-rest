package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityFullException;

/**
 * Rejects a create-owner request whose city already contains {@value #CAPACITY} or more owners
 * with a 409. The city is compared case-insensitively, matching the per-city counting used by
 * {@code AssignOwnerCustomerCode}. Runs before the owner is saved, so the count reflects
 * existing owners only.
 */
public class CheckOwnerCityCapacity {

    /** A city is full once it already holds this many owners. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityFullException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(existing -> city == null ? existing == null : city.equalsIgnoreCase(existing))
                .count();
        if (count >= CAPACITY) {
            throw new OwnerCityFullException(city, (int) count);
        }
    }
}
