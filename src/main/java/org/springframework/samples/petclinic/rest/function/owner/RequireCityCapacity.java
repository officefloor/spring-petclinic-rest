package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create request whose city already contains {@value #CAPACITY} or more owners
 * (compared case-insensitively). At or above capacity rejects 409.
 */
public class RequireCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(owner -> city != null && city.equalsIgnoreCase(owner.getCity()))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
