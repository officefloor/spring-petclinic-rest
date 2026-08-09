package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner when the owner's city already contains {@value #CITY_OWNER_LIMIT} or
 * more owners, comparing cities case-insensitively. Runs before {@link BuildOwner}/{@link SaveOwner}
 * so the clash is a 409 (Conflict) rather than a persisted row.
 */
public class EnsureOwnerCityCapacity {

    /** Maximum number of owners permitted in a single city. */
    static final int CITY_OWNER_LIMIT = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city == null ? existing.getCity() == null : city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_OWNER_LIMIT) {
            throw new CityAtCapacityException(city, CITY_OWNER_LIMIT);
        }
    }
}
