package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create when the owner's city already holds {@value #CAPACITY} or more owners
 * (compared case-insensitively with collapsed whitespace).
 */
public class EnsureOwnerCityWithinCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = key(owner.getCity());
        long count = ownerRepository.findAll().stream()
                .filter(other -> key(other.getCity()).equals(city))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException("This city already has the maximum number of owners");
        }
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
