package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose city already holds {@link Cities#MAX_OWNERS_PER_CITY} owners.
 * The existing owners in the request's city are counted with the same case- and
 * whitespace-insensitive matching used elsewhere (see {@link Cities}), so a city at capacity
 * cannot grow further.
 */
public class EnsureCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        if (Cities.isAtCapacity(ownerRepository, request.getCity())) {
            throw new CityAtCapacityException(request.getCity());
        }
    }
}
