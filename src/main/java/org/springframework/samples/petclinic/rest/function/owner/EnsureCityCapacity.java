package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityCapacityExceededException;

/**
 * Rejects a create request whose city already holds the maximum number of owners. Runs
 * before {@link BuildOwner}, so a full city is a 409 via
 * {@link CityCapacityExceededException} before any owner is built or saved. The capacity
 * policy lives in {@link CityCapacity}.
 */
public class EnsureCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityCapacityExceededException {
        if (CityCapacity.isFull(ownerRepository, request.getCity())) {
            throw new CityCapacityExceededException(request.getCity(), CityCapacity.MAX_OWNERS_PER_CITY);
        }
    }
}
