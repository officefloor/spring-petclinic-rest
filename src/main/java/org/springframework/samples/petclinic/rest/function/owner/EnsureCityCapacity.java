package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create whose city already contains 50 or more owners. The city is compared
 * case-insensitively, matching {@link AssignCustomerCode}'s per-city counting. Runs
 * before the owner is saved, so the count excludes the owner being created; a full city
 * is rejected 409 via {@link CityAtCapacityException}.
 */
public class EnsureCityCapacity {

    /** Maximum number of owners a single city may contain. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city, count);
        }
    }
}
