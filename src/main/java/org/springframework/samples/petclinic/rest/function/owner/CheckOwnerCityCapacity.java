package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request when the request's city already contains 50 or more owners,
 * throwing {@link CityAtCapacityException} for a 409. The city is compared case-insensitively. Runs
 * before the owner is saved, so the count excludes the owner being created.
 */
public class CheckOwnerCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
