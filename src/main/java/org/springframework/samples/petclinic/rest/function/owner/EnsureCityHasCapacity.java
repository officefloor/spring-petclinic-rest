package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * On create, rejects the owner when its city already contains 50 or more owners (compared
 * case-insensitively with collapsed whitespace).
 */
public class EnsureCityHasCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(owner.getCity());
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CAPACITY) {
            throw new CityAtCapacityException("This city already has the maximum number of owners");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
