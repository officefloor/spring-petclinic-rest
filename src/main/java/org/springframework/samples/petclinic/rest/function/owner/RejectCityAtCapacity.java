package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects creating an owner when the owner's city already contains 50 or more existing
 * owners. Cities are compared case-insensitively on their trimmed value. Runs before
 * {@link SaveOwner}, so the owner being created is not counted. A city at (or over)
 * capacity escalates as a 409 via {@link CityAtCapacityException}.
 */
public class RejectCityAtCapacity {

    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = owner.getCity() == null ? "" : owner.getCity().trim();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> existing.getCity() != null
                        && city.equalsIgnoreCase(existing.getCity().trim()))
                .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException(
                    "City '" + owner.getCity() + "' already contains " + CITY_CAPACITY
                            + " or more owners");
        }
    }
}
