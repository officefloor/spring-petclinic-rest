package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CrowdedCityException;

/**
 * Rejects a create body whose city already holds 50 or more owners (compared
 * case-insensitively), so the endpoint responds 409.
 */
public class RejectCrowdedCity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CrowdedCityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(o -> city.equalsIgnoreCase(o.getCity()))
                .count();
        if (count >= CAPACITY) {
            throw new CrowdedCityException(city);
        }
    }
}
