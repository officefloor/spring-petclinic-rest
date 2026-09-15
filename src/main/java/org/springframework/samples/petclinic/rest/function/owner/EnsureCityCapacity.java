package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityCapacityExceededException;

/**
 * Rejects a create request whose city already holds the maximum number of owners. Runs
 * before {@link BuildOwner}, so a full city is a 409 via
 * {@link CityCapacityExceededException} before any owner is built or saved. Cities are
 * counted case-insensitively (see {@link Cities}).
 */
public class EnsureCityCapacity {

    /** Maximum owners a single city may hold; the next create is rejected. */
    static final long MAX_OWNERS_PER_CITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityCapacityExceededException {
        if (Cities.countIn(ownerRepository, request.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityExceededException(request.getCity(), MAX_OWNERS_PER_CITY);
        }
    }
}
