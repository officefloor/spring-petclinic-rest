package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create request whose city (compared case-insensitively) already holds
 * {@value #CAPACITY} or more owners, so a full city is a 409 via {@link CityAtCapacityException}.
 */
public class EnsureCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long inCity = ownerRepository.findAll().stream()
            .filter(owner -> city != null && city.equalsIgnoreCase(owner.getCity()))
            .count();
        if (inCity >= CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
