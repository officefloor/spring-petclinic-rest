package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already contains 50 or more owners, so the escalation
 * handler can respond 409. City is compared case-insensitively, matching how {@link AssignCustomerCode}
 * counts a city's owners. Runs within the same write transaction as the insert so the count reflects
 * only owners already persisted (excluding this new, not-yet-saved one), giving each city a hard cap
 * of 50 owners.
 */
public class CheckCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
