package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner when the owner's city already contains
 * {@value OwnerCityCapacity#CITY_OWNER_LIMIT} or more owners, comparing cities case-insensitively.
 * Runs before {@link BuildOwner}/{@link SaveOwner} so the clash is a 409 (Conflict) rather than a
 * persisted row. The counting and the companion read-time warning live in {@link OwnerCityCapacity}.
 */
public class EnsureOwnerCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        if (OwnerCityCapacity.isAtCapacity(ownerRepository, city)) {
            throw new CityAtCapacityException(city, OwnerCityCapacity.CITY_OWNER_LIMIT);
        }
    }
}
