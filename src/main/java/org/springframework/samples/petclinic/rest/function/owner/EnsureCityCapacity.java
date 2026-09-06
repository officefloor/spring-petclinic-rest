package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects creating an owner when the owner's city already contains 50 or more owners. The city is
 * compared case-insensitively with runs of whitespace collapsed to a single space. The owner being
 * created is not yet persisted, so it is not part of the count.
 */
public class EnsureCityCapacity {

    private static final int MAX_OWNERS_PER_CITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(owner.getCity());
        long count = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> normalize(existing.getCity()).equals(city))
                .count();
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new CityAtCapacityException(
                    "The city " + owner.getCity() + " already has " + MAX_OWNERS_PER_CITY + " or more owners");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
