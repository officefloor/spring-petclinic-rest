package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a new owner whose city already contains {@link #CAPACITY} or more owners, so the request is
 * answered with a 409 Conflict rather than exceeding the per-city capacity. Cities are compared in
 * their {@link OwnerCity#normalize normalized} form (whitespace collapsed, lower-cased), consistent
 * with the other create-time checks. Runs after {@link CheckIdentityUnique} and before
 * {@link BuildOwner}.
 */
public class CheckCityCapacity {

    /** Maximum number of owners permitted per city. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        if (OwnerCity.countInCity(city, ownerRepository) >= CAPACITY) {
            throw new CityAtCapacityException(city, CAPACITY);
        }
    }
}
