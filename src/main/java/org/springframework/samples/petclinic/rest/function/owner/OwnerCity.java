package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single source of truth for what a "normalized" owner city is, used to compare and count owners by
 * city. Mirrors the normalization of the other cross-owner comparison helpers
 * ({@link Household}, {@link OwnerAddress#normalize}): surrounding and internal whitespace is collapsed
 * to a single space and the value is lower-cased locale-independently, so "Sydney" / " sydney " and
 * "New  York" / "New York" count as the same city.
 *
 * <p>Unlike address and telephone, the city is not stored in a canonical form, so this is only a
 * comparison aid; its consumers are the per-city {@link CheckCityCapacity capacity check} and the
 * {@link FlagCityCapacityWarning capacity warning}, both reached through {@link #countInCity}, and there
 * is no stored value to drift from.
 */
final class OwnerCity {

    private OwnerCity() {
    }

    /**
     * Returns the normalized form of {@code city}, or the empty string when it is {@code null} or
     * blank after trimming.
     */
    static String normalize(String city) {
        if (city == null) {
            return "";
        }
        return city.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Counts the owners whose city matches {@code city} in its {@link #normalize normalized} form — the
     * single source of the per-city owner count shared by the capacity check and the capacity warning.
     */
    static int countInCity(String city, OwnerRepository ownerRepository) {
        String normalizedCity = normalize(city);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalizedCity.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count;
    }
}
