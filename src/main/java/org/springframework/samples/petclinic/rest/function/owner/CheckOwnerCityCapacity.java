package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already contains 50 or more owners (city compared
 * case-insensitively). Runs before the owner is saved, so the count excludes the owner being
 * created. Throws {@link CityAtCapacityException} (handled as 409) when the city is at capacity.
 */
public class CheckOwnerCityCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        int count = (int) ownerRepository.findAll().stream()
            .filter(o -> city.equalsIgnoreCase(o.getCity()))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city, count);
        }
    }
}
