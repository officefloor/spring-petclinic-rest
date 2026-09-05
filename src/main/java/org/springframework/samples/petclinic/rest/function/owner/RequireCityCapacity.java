package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already holds {@value #CAPACITY} or more owners,
 * before {@link BuildOwner} runs. Cities are compared exactly, as elsewhere in the pipeline.
 */
public class RequireCityCapacity {

    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
            .filter(existing -> city.equals(existing.getCity()))
            .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
