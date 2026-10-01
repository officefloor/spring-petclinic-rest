package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request when the owner's city is already at capacity.
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} and before {@link BuildOwner}. A city is at
 * capacity once it already contains 50 or more owners; the 51st owner in that city is refused.
 * The city is matched exactly. A match throws {@link OwnerCityAtCapacityException}, handled as a
 * 409 Conflict.
 */
public class CheckOwnerCityCapacity {

    /** Maximum number of owners permitted per city; the next owner is rejected. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityAtCapacityException {
        String city = request.getCity();
        long ownersInCity = ownerRepository.findAll().stream()
                .filter(existing -> city.equals(existing.getCity()))
                .count();
        if (ownersInCity >= CITY_CAPACITY) {
            throw new OwnerCityAtCapacityException(city);
        }
    }
}
