package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose city already holds {@link #MAX_OWNERS_PER_CITY} owners. The
 * existing owners in the request's city are counted with the same case- and whitespace-insensitive
 * matching used elsewhere (see {@link Cities#matches}), so a city at capacity cannot grow further.
 */
public class EnsureCityCapacity {

    /** Maximum number of owners allowed per city; the next create in a full city is rejected. */
    static final int MAX_OWNERS_PER_CITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        int count = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (Cities.matches(owner, request.getCity())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(request.getCity());
        }
    }
}
