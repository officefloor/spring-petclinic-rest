package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Per-city capacity thresholds and counting, shared by the hard-limit create step
 * ({@link CheckCityCapacity}) and the {@code capacityWarning} response flag so both read the same
 * count the same way. City names are compared case-insensitively with surrounding whitespace trimmed.
 */
public final class CityCapacity {

    /** Maximum owners permitted per city; the next create in a full city is rejected. */
    static final int MAX_PER_CITY = 50;

    /** Owners in a city from this count up to {@link #MAX_PER_CITY} - 1 raise the capacity warning. */
    static final int WARN_FROM = 40;

    private CityCapacity() {
    }

    /** Number of owners whose city matches {@code city} (case-insensitive, whitespace-trimmed). */
    public static int countInCity(String city, OwnerRepository ownerRepository) {
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
     * Whether the owner's city is approaching capacity: it already holds between {@value #WARN_FROM}
     * and {@code MAX_PER_CITY - 1} owners (inclusive). A full city (at {@value #MAX_PER_CITY}) reads as
     * at-capacity, not approaching, and warns false.
     */
    public static boolean warningFor(Owner owner, OwnerRepository ownerRepository) {
        int count = countInCity(owner.getCity(), ownerRepository);
        return count >= WARN_FROM && count < MAX_PER_CITY;
    }

    /** Trims and lower-cases so comparison ignores case and surrounding spacing. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
