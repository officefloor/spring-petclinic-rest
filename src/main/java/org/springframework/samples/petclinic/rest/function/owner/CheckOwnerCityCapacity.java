package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityFullException;

/**
 * Rejects a create-owner request whose city is already at capacity: a city that already contains
 * 50 or more owners cannot take another. The city is compared case-insensitively (matching how
 * {@link AssignCustomerCode} counts a city's owners). Runs after {@link ValidateNewOwner} has
 * published the request, and before {@link BuildOwner}, so a full city is a 409 rather than a
 * persisted record.
 */
public class CheckOwnerCityCapacity {

    /** Maximum number of owners permitted in a single city. */
    private static final long CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityFullException {
        String city = request.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (inCity >= CITY_CAPACITY) {
            throw new OwnerCityFullException(city);
        }
    }
}
