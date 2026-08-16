package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects creating an owner when the owner's city already contains {@link CityCapacity#MAX_PER_CITY}
 * or more owners, so the create endpoint responds 409 instead of overfilling the city. City names are
 * compared case-insensitively with surrounding whitespace trimmed. Runs after {@link ValidateOwner}
 * (which publishes the body) and before {@link BuildOwner}, counting every existing owner. The softer
 * {@code capacityWarning} response flag for a city approaching this limit is {@link CityCapacity}.
 */
public class CheckCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        int count = CityCapacity.countInCity(request.getCity(), ownerRepository);
        if (count >= CityCapacity.MAX_PER_CITY) {
            throw new CityAtCapacityException(request.getCity(), count);
        }
    }
}
