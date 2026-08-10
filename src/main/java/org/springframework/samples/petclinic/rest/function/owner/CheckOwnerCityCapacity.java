package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityCapacityConflictException;

/**
 * Runs before {@link BuildOwner}: rejects the request with 409 when the owner's city already
 * contains 50 or more owners (the per-city capacity). The city is compared case-insensitively,
 * matching how the customer code's per-city sequence is derived.
 */
public class CheckOwnerCityCapacity {

    private static final long CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityCapacityConflictException {
        String city = request.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (inCity >= CITY_CAPACITY) {
            throw new OwnerCityCapacityConflictException(
                    "The city " + city + " has reached its capacity of " + CITY_CAPACITY + " owners");
        }
    }
}
