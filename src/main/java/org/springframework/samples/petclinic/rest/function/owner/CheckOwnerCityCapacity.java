package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already contains {@value #CAPACITY} or more owners,
 * with a 409 Conflict. City is compared case-insensitively (matching the customer-code rule).
 * Runs after {@link BuildOwner} and before the owner is saved, so the count reflects only
 * pre-existing owners.
 */
public class CheckOwnerCityCapacity {

    /** Maximum number of owners a single city may hold. */
    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not counted against the city
            }
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city, count);
        }
    }
}
