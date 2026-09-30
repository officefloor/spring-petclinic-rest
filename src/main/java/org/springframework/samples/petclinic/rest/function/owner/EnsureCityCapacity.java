package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create whose city already contains 50 or more owners. Reads the per-city total
 * published by {@link CountCityOwners}, which is taken before the owner is saved so the count
 * excludes the owner being created; a full city is rejected 409 via
 * {@link CityAtCapacityException}.
 */
public class EnsureCityCapacity {

    /** Maximum number of owners a single city may contain. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request,
            @Qualified("cityOwnerCount") @Val Integer cityOwnerCount)
            throws CityAtCapacityException {
        if (cityOwnerCount >= CITY_CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), cityOwnerCount);
        }
    }
}
