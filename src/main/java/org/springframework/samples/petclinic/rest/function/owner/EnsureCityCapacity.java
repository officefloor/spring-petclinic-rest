package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request when the request's city already contains the maximum number of
 * owners ({@link CityAtCapacityException#CAPACITY}). Existing owners are counted with their city
 * compared case-insensitively and trimmed. Runs before {@link SaveOwner}, so the new owner is not
 * yet counted; a city holding 50 or more owners causes the 51st request to fail with 409.
 */
public class EnsureCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CityAtCapacityException.CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), count);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
