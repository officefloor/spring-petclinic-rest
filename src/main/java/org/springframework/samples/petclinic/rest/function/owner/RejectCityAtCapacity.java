package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has published the
 * request. Rejects the request when the named city already contains {@value #MAX_OWNERS_PER_CITY}
 * or more owners, so a full city is a 409 Conflict rather than an over-capacity create. City
 * matching is case-insensitive, consistent with {@link AssignCustomerCode}.
 */
public class RejectCityAtCapacity {

    /** Maximum number of owners a single city may contain. */
    static final int MAX_OWNERS_PER_CITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
                .count();
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
