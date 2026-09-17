package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create request whose city has already reached capacity: a city is full once it holds
 * {@link #CAPACITY} or more existing owners, compared case-insensitively (as {@link AssignCustomerCode}
 * counts a city). A full city is a 409 via {@link CityAtCapacityException}.
 */
public class EnsureCityCapacity {

    /** Maximum number of owners a single city may hold. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long existing = ownerRepository.findAll().stream()
                .filter(owner -> city.equalsIgnoreCase(owner.getCity()))
                .count();
        if (existing >= CAPACITY) {
            throw new CityAtCapacityException(city, CAPACITY);
        }
    }
}
