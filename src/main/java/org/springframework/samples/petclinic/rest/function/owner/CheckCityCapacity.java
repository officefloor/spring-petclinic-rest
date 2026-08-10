package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already contains {@value #CITY_CAPACITY} or more
 * owners. City is compared case-insensitively, matching {@link AssignCustomerCode}. A city at
 * capacity is reported as a 409 (see {@link CityAtCapacityException}).
 *
 * <p>Runs after {@link ValidateOwnerFields} (which republishes the validated request as a variable)
 * and before {@link BuildOwner}, so a rejected request is never persisted.
 */
public class CheckCityCapacity {

    /** Maximum owners permitted per city. */
    static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city, CITY_CAPACITY);
        }
    }
}
