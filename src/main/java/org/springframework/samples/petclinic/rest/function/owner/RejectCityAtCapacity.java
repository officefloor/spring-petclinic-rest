package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose city already contains {@link #CITY_CAPACITY} or more owners.
 * Runs before the owner is built and saved. Cities are matched case-insensitively after trimming and
 * collapsing runs of whitespace to a single space. A city at capacity is rejected via
 * {@link CityAtCapacityException}, which the global handler turns into a 409.
 */
public class RejectCityAtCapacity {

    /** Maximum number of owners allowed per city; the request is rejected once this many already exist. */
    static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), CITY_CAPACITY);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
