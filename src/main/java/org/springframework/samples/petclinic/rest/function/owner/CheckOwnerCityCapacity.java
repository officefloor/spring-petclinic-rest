package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityCapacityException;

/**
 * Rejects a create-owner request whose city has reached capacity — it already contains 50 or more
 * owners (compared case-insensitively). Throws {@link OwnerCityCapacityException} (handled as 409)
 * when the limit is met or exceeded.
 */
public class CheckOwnerCityCapacity {

    /** The maximum number of owners permitted in a single city. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new OwnerCityCapacityException(city, count);
        }
    }
}
