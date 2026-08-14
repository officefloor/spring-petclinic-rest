package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Runs in the create-owner pipeline before {@link BuildOwner}. Counts existing owners whose city
 * matches the request's city (case-insensitively) and rejects the request with a 409 when that
 * count has already reached the per-city capacity of 50, so a full city cannot register any further
 * owners.
 */
public class EnsureCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city, (int) count);
        }
    }
}
