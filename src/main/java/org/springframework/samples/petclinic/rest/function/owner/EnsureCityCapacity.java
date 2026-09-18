package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already holds the maximum number of owners, before
 * {@link BuildOwner} runs. A city is full once it contains {@value #CAPACITY} or more owners, so the
 * next one would be the ({@value #CAPACITY}+1)th. Rejects with 409 otherwise.
 */
public class EnsureCityCapacity {

    /** Maximum owners a single city may hold; the next create is rejected. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        long count = ownerRepository.countByCity(request.getCity());
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), count);
        }
    }
}
