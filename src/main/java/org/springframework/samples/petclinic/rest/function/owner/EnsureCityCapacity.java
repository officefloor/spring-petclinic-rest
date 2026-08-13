package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose {@code city} already contains 50 or more owners,
 * responding 409 via {@link CityAtCapacityException}. Cities are compared
 * case-insensitively.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which publishes the request body) and
 * before {@link BuildOwner} saves anything, so the count it reads excludes the owner
 * being created — the 51st owner in a city is the first one rejected.
 */
public class EnsureCityCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long inCity = ownerRepository.findAll().stream()
            .filter(existing -> city == null
                ? existing.getCity() == null
                : city.equalsIgnoreCase(existing.getCity()))
            .count();
        if (inCity >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
