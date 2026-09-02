package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create request whose city already contains {@value #CAPACITY} or more owners, comparing
 * cities case-insensitively with collapsed whitespace. Responds 409 on a full city.
 */
public class RejectCityAtCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (city.equals(normalize(owner.getCity())) && ++count >= CAPACITY) {
                throw new CityAtCapacityException(request.getCity());
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
