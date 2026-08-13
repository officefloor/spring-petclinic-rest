package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared accumulation path for the per-city owner rules. Counts the owners whose city matches a
 * given city case-insensitively with collapsed whitespace, the same bucket used by both the hard
 * per-city capacity limit ({@link CheckOwnerCityCapacity}) and the approaching-capacity warning
 * surfaced on {@link org.springframework.samples.petclinic.rest.dto.OwnerDto}.
 */
final class OwnerCityCounts {

    /** Maximum owners allowed per city; the next create in a full city is rejected. */
    static final int MAX_OWNERS_PER_CITY = 50;

    /** At this many owners (up to but excluding {@link #MAX_OWNERS_PER_CITY}) a city warns. */
    static final int WARNING_THRESHOLD = 40;

    private OwnerCityCounts() {
    }

    /** Number of owners in {@code city}, matched case- and whitespace-insensitively. */
    static int countInCity(OwnerRepository ownerRepository, String city) {
        String normalized = normalize(city);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalized.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count;
    }

    /**
     * True when the owner's city already holds between {@link #WARNING_THRESHOLD} and one below
     * {@link #MAX_OWNERS_PER_CITY} owners (inclusive), i.e. it is approaching but not at the limit.
     */
    static boolean capacityWarning(OwnerRepository ownerRepository, Owner owner) {
        int count = countInCity(ownerRepository, owner.getCity());
        return count >= WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY;
    }

    /**
     * True when the owner's city is over its soft capacity, i.e. it already holds at least
     * {@link #WARNING_THRESHOLD} owners (the soft, per-city warning threshold), matched case- and
     * whitespace-insensitively. Unlike {@link #capacityWarning}, this stays true once the city
     * reaches the hard {@link #MAX_OWNERS_PER_CITY} limit.
     */
    static boolean overSoftCapacity(OwnerRepository ownerRepository, Owner owner) {
        return countInCity(ownerRepository, owner.getCity()) >= WARNING_THRESHOLD;
    }

    /** Lower-case and collapse runs of whitespace so comparison is case- and whitespace-insensitive. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
