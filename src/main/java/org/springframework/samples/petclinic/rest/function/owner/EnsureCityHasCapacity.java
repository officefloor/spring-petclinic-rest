package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Runs in {@code POST /api/owners} after the uniqueness checks, reading the already-validated
 * body as a variable. Rejects the request when its city (compared case-insensitively) already
 * holds {@link #CITY_CAPACITY} or more owners, throwing {@link CityAtCapacityException} for a
 * 409 before any entity is built or persisted. Reads the count in the same transaction as the
 * writes.
 */
public class EnsureCityHasCapacity {

    /** Maximum number of owners a single city may hold. */
    static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {

        long cityCount = ownerRepository.findAll().stream()
                .filter(existing -> request.getCity().equalsIgnoreCase(existing.getCity()))
                .count();
        if (cityCount >= CITY_CAPACITY) {
            throw new CityAtCapacityException(request.getCity());
        }
    }
}
