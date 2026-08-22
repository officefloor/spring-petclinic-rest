package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's {@code capacityWarning}: true when the owner's city already contains between 40
 * and 49 owners (inclusive), signalling that the city is approaching the hard capacity limit of 50
 * enforced by {@link EnsureCityCapacity}. Existing owners are counted by city, compared
 * case-insensitively with collapsed whitespace, mirroring the capacity rule so the warning boundary
 * lines up exactly with the rejection. Runs before {@code save}, so the new owner is not yet counted.
 */
public class AssignCapacityWarning {

    private static final int WARNING_THRESHOLD = 40;

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

    /** Case-insensitive with collapsed whitespace: trim, fold internal whitespace runs to a
     *  single space, and lower-case. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
