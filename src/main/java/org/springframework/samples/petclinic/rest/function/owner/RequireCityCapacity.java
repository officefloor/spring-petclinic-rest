package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Step of {@code POST /api/owners} that rejects a create when the owner's city already contains 50
 * or more owners, throwing {@link CityAtCapacityException} (handled as 409 Conflict). Cities are
 * compared case-insensitively with trimmed whitespace. Runs after {@link RequireOwnerFields} has
 * published the validated body and before {@link BuildOwner}/{@link SaveOwner} persist the new
 * owner.
 */
public class RequireCityCapacity {

    /** Maximum number of owners permitted in a single city. */
    private static final int CITY_CAPACITY = 50;

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
            throw new CityAtCapacityException(
                    "City " + request.getCity() + " already has the maximum of " + CITY_CAPACITY
                            + " owners");
        }
    }

    private static String normalize(String city) {
        return city == null ? "" : city.trim().toLowerCase();
    }
}
