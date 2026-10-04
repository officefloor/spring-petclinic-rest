package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time capacity guard for {@code POST /owners}: rejects a request whose city already
 * contains 50 or more owners, with a 409 via {@link CityAtCapacityException}.
 *
 * <p>The city is compared case-insensitively against each existing owner's city (as in
 * {@link AssignCustomerCode}), so the count is independent of letter case. Runs before
 * {@link BuildOwner}.
 */
public class CheckOwnerCityCapacity {

    /** Maximum number of owners permitted in a single city. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
            .map(Owner::getCity)
            .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
