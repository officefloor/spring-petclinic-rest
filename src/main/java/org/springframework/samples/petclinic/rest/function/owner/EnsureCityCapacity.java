package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create request whose city already holds the maximum number of owners (50), so a single
 * city cannot grow without bound. City is compared case-insensitively with collapsed whitespace.
 * Runs before {@link BuildOwner}; a city already at capacity is a 409 via
 * {@link CityAtCapacityException}.
 */
public class EnsureCityCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getCity()).equals(city)) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(request.getCity());
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
