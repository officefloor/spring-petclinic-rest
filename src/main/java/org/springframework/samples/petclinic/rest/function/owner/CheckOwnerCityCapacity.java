package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time capacity guard for {@code POST /owners}: rejects a request whose city already
 * contains 50 or more owners, with a 409 via {@link CityAtCapacityException}.
 *
 * <p>The city is compared case-insensitively against each existing owner's city (as in
 * {@link AssignMemberId}), so the count is independent of letter case. Runs before
 * {@link BuildOwner}.
 */
public class CheckOwnerCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        if (CityCapacity.countOwnersIn(city, ownerRepository) >= CityCapacity.CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
