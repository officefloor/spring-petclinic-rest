package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a new owner whose city already contains {@value #CAPACITY} or more owners, comparing city
 * names case-insensitively. Runs before Save so a full city is a 409, not a persisted overflow.
 */
public class EnsureCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws CityAtCapacityException {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city, count);
        }
    }
}
