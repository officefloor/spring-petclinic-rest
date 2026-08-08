package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityAtCapacityException;

/**
 * Rejects a new owner whose city has reached capacity: a city that already contains
 * {@value #CITY_CAPACITY} or more owners accepts no more. Cities are compared in a
 * normalized form — trimmed, internal whitespace runs collapsed to a single space,
 * and lower-cased. Throws a checked {@link OwnerCityAtCapacityException} (turned into
 * a 409 Conflict by the escalation handler) when the limit is met.
 */
public class RejectOwnerCityAtCapacity {

    /** Maximum number of owners allowed to share a city. */
    static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerCityAtCapacityException {
        String city = normalize(owner.getCity());
        if (city == null) {
            return; // no city to count against
        }
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never count the owner itself
            }
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new OwnerCityAtCapacityException(
                    "City '" + owner.getCity() + "' already has the maximum number of owners");
        }
    }

    /** City key: lower-cased, trimmed, with internal whitespace runs collapsed to one space. */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        return collapsed.isEmpty() ? null : collapsed.toLowerCase();
    }
}
