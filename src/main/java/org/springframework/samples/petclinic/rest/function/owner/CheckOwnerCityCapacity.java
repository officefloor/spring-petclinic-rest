package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request when the owner's city is already at capacity.
 *
 * <p>Runs after {@link ValidateOwnerFields} and before {@link BuildOwner}. A city is at
 * capacity once it already contains {@link OwnerCities#CITY_CAPACITY} or more owners; the next owner
 * in that city is refused. The city is matched exactly over the shared {@link OwnerCities#countInCity}
 * count. A match throws {@link OwnerCityAtCapacityException}, handled as a 409 Conflict.
 */
public class CheckOwnerCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityAtCapacityException {
        String city = request.getCity();
        long ownersInCity = OwnerCities.countInCity(ownerRepository, city);
        if (ownersInCity >= OwnerCities.CITY_CAPACITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }
}
