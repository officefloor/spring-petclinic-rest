package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityAtCapacityException;

/**
 * Rejects a create request when the owner's city already contains 50 or more owners,
 * comparing city names case-insensitively with collapsed whitespace. Runs after
 * {@link ValidateOwnerFields}, so it sees the validated request, and before
 * {@link BuildOwner}, so an at-capacity city is a 409 rather than a persisted record.
 */
public class CheckOwnerCityCapacity {

    /** Cities at or above this many owners reject further creates. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new OwnerCityAtCapacityException(request.getCity(), count);
        }
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
