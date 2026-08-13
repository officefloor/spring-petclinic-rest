package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs on {@code POST /api/owners} before {@link BuildOwner} (so no owner is created on conflict):
 * rejects the request with 409 via {@link CityAtCapacityException} when the request's city already
 * contains {@link OwnerCityCounts#MAX_OWNERS_PER_CITY} or more owners, compared case-insensitively
 * with collapsed whitespace.
 */
public class CheckOwnerCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        if (OwnerCityCounts.countInCity(ownerRepository, request.getCity())
                >= OwnerCityCounts.MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(request.getCity());
        }
    }
}
