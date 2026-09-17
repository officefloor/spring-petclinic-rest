package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already holds the maximum number of owners
 * ({@value #CAPACITY}), responding 409 via {@link CityAtCapacityException}. Cities are
 * compared exactly.
 */
public class RequireCityCapacity {

    /** Maximum owners allowed per city; the request is rejected once this is reached. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        if (OwnerCities.countIn(ownerRepository, city) >= CAPACITY) {
            throw new CityAtCapacityException(city, CAPACITY);
        }
    }
}
