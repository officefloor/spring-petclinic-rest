package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Create-owner step: rejects the request with a 409 when the new owner's city already holds
 * {@link #CAPACITY} or more owners (compared case-insensitively, see {@link SameCity}). Runs
 * after {@link ValidateOwnerFields} (so the city is present) and before {@link BuildOwner}.
 */
public class EnsureCityHasCapacity {

    /** The maximum number of owners a single city may hold. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        if (SameCity.count(ownerRepository.findAll(), request.getCity()) >= CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), CAPACITY);
        }
    }
}
