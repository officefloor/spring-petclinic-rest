package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when the owner's city already
 * held between 40 and 49 owners at the moment this owner was created (approaching the hard
 * capacity limit of 50), {@code false} otherwise. Cities are compared case-insensitively
 * with collapsed whitespace, matching {@link CheckOwnerCityCapacity}. Runs after
 * {@link BuildOwner} (so the city is set) and before {@link SaveOwner} (so the count
 * reflects the owners that existed before this create and excludes the one being created).
 * The 50-owner hard rejection has already run in {@link CheckOwnerCityCapacity}, so the
 * count here is never 50 or more. The value is fixed at creation time and persisted with
 * the owner.
 */
public class AssignCapacityWarning {

    /** Cities already holding at least this many owners raise the warning. */
    private static final int WARNING_THRESHOLD = 40;

    /** Cities at or above this many owners are already rejected before this step. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CITY_CAPACITY);
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
