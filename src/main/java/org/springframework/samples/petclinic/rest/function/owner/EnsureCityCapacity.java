package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects creating an owner when the owner's city already contains 50 or more owners.
 * The count is taken over the existing owners (the new owner is not yet saved), matching
 * cities by exact value as {@link AssignCustomerCode} does. At 50 or more existing owners
 * in the city the request is a 409 Conflict (see {@link CityAtCapacityException}).
 */
public class EnsureCityCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long inCity = ownerRepository.findAll().stream()
            .filter(existing -> city != null && city.equals(existing.getCity()))
            .count();
        if (inCity >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city, inCity);
        }
    }
}
