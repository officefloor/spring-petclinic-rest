package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects creating an owner when the owner's city already contains
 * {@link CityCapacity#CITY_CAPACITY} or more owners. The count comes from
 * {@link CityCapacity#countInCity} (the same single source of truth the capacity warning
 * in {@link AssessCityCapacity} uses), taken over the existing owners (the new owner is
 * not yet saved). At the limit or above, the request is a 409 Conflict (see
 * {@link CityAtCapacityException}).
 */
public class EnsureCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long inCity = CityCapacity.countInCity(ownerRepository, city);
        if (inCity >= CityCapacity.CITY_CAPACITY) {
            throw new CityAtCapacityException(city, inCity);
        }
    }
}
