package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Runs before {@link BuildOwner}. Rejects the request when the owner's city already contains the
 * maximum number of owners (50), by throwing {@link CityAtCapacityException} (handled as 409). Cities
 * are compared case-insensitively with collapsed whitespace so differently-cased spellings count
 * towards the same city.
 */
public class EnsureCityBelowCapacity {

    /** The maximum number of owners permitted per city. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = canonical(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(canonical(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), count);
        }
    }

    /** Lower-cased, trimmed, with any run of whitespace collapsed to a single space. */
    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
