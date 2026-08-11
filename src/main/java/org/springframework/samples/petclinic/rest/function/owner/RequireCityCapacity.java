package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Step of {@code POST /api/owners}: rejects the request 409 when the owner's city already contains
 * 50 or more owners. City is compared exactly, consistent with the region derivation in
 * {@link AssignMemberId}. Runs before {@link BuildOwner} so no owner is persisted on conflict.
 */
public class RequireCityCapacity {

    /** Maximum number of owners permitted in a single city. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
