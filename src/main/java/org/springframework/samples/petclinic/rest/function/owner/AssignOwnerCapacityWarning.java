package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags an owner whose city is approaching the capacity limit. Counts the other owners
 * already sharing this owner's city and sets {@code capacityWarning} to true when that
 * count falls in the warning band {@value #WARNING_THRESHOLD}..{@value #CITY_CAPACITY}-1
 * (40-49). The hard rejection at {@value #CITY_CAPACITY} is handled separately by
 * {@link RejectOwnerCityAtCapacity}; this rule never rejects.
 *
 * <p>Cities are compared in the same normalized form the rejection rule uses — trimmed,
 * internal whitespace runs collapsed to a single space, and lower-cased. The value is
 * recomputed each time this step runs (on create and on read), so it always reflects the
 * city's current membership.
 */
public class AssignOwnerCapacityWarning {

    /** Other owners in the city must reach this count for the warning to be raised. */
    static final int WARNING_THRESHOLD = 40;

    /** Hard capacity limit; a city with this many owners accepts no more (see rejection rule). */
    static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        if (city == null) {
            owner.setCapacityWarning(false);
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
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CITY_CAPACITY);
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
