package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags a city approaching its capacity limit.
 * {@code capacityWarning} is set to {@code true} when, at the moment this owner is created, the
 * owner's city already holds between {@value #WARNING_THRESHOLD} and {@value #CITY_CAPACITY}-1
 * owners (approaching the hard limit of {@value #CITY_CAPACITY}, enforced separately by
 * {@link RequireCityCapacity}); otherwise {@code false}. The city is compared case-insensitively
 * with collapsed whitespace, the same canonical form used by {@link RequireCityCapacity}. Runs
 * before {@link SaveOwner}, so the owner being created is not counted against itself.
 */
public class AssignCapacityWarning {

    /** Maximum number of owners a single city may contain (the hard limit). */
    private static final int CITY_CAPACITY = 50;

    /** Owner count in a city at or above which the approaching-capacity warning is flagged. */
    private static final int WARNING_THRESHOLD = 40;

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

    /** Trim, collapse internal whitespace runs to a single space, and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
