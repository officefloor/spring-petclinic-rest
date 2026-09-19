package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose city already holds the maximum number of owners
 * ({@value #CAPACITY}), responding 409, before {@link BuildOwner} persists one over the
 * limit. City membership is counted via {@link Cities}.
 */
public class EnsureCityUnderCapacity {

    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        if (Cities.countIn(ownerRepository, request.getCity()) >= CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), CAPACITY);
        }
    }
}
