package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the read-time {@code capacityWarning} flag on an owner: {@code true} once the owner's
 * city already holds between {@link #WARN_THRESHOLD} and {@link CheckOwnerCityCapacity#CAPACITY}
 * minus one owners (inclusive) — i.e. it is approaching, but has not yet reached, the hard
 * per-city capacity enforced by {@link CheckOwnerCityCapacity}. Otherwise {@code false}. Cities
 * are compared case-insensitively with surrounding whitespace trimmed, matching the hard-limit
 * rule. Runs before the responder so the flag is carried onto the returned DTO.
 */
public class MarkCityCapacityWarning {

    /** The warning is raised once the city holds at least this many owners. */
    public static final int WARN_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARN_THRESHOLD && count < CheckOwnerCityCapacity.CAPACITY);
    }

    /** Lower-case and trim, treating null as empty, for case-insensitive city comparison. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
