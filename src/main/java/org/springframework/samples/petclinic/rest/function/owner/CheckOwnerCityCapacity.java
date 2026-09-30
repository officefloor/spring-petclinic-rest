package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerLimitExceededException;

/**
 * Rejects creating an owner when the owner's city already holds {@value #CITY_CAPACITY} or more
 * owners. A city at capacity is rejected 409 via {@link CityOwnerLimitExceededException}.
 */
public class CheckOwnerCityCapacity {

    static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityOwnerLimitExceededException {
        String city = request.getCity();
        if (countInCity(ownerRepository, city) >= CITY_CAPACITY) {
            throw new CityOwnerLimitExceededException(
                    "City " + city + " already has " + CITY_CAPACITY + " or more owners");
        }
    }

    /** Existing owners whose city equals {@code city} case-insensitively, backing the 409 gate. */
    private static long countInCity(OwnerRepository ownerRepository, String city) {
        return ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
    }
}
