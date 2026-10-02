package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Runs after {@link ValidateUniqueHousehold} and before {@link BuildOwner}. A read-only
 * validation gate: rejects the create with a 409 when the owner's city already contains 50
 * or more owners (compared case-insensitively), so no city grows past capacity. Publishes
 * nothing; it only escalates on conflict.
 */
public class ValidateCityCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(
                    "City " + city + " already contains " + CITY_CAPACITY + " or more owners");
        }
    }
}
