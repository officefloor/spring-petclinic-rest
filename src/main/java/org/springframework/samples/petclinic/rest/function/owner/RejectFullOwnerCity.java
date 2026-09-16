package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already holds {@link #CITY_CAPACITY} or more
 * owners, responding 409. City membership is counted case-insensitively via
 * {@link OwnerCities}.
 */
public class RejectFullOwnerCity {

    /** Maximum number of owners a single city may hold. */
    static final int CITY_CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        if (OwnerCities.size(ownerRepository.findAll(), request.getCity()) >= CITY_CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), CITY_CAPACITY);
        }
    }
}
