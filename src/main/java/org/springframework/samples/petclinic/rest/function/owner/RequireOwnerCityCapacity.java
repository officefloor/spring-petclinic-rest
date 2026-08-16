package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityCapacityExceededException;

/**
 * Rejects the create when the owner's {@code city} already contains 50 or more owners,
 * compared case-insensitively (the same match used by the per-city customer-code sequence).
 * A full city is rejected 409 via {@link OwnerCityCapacityExceededException}.
 *
 * <p>Runs before {@link SaveOwner}, so the count reflects only the existing owners, not the
 * one being created.
 */
public class RequireOwnerCityCapacity {

    /** A city may hold at most this many owners. */
    static final long CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityCapacityExceededException {
        String city = request.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (inCity >= CITY_CAPACITY) {
            throw new OwnerCityCapacityExceededException(city);
        }
    }
}
