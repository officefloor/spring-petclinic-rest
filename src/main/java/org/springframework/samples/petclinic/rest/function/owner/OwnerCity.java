package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for what a "normalized" owner city is, used to compare cities for the
 * per-city capacity check. Mirrors the normalization of the other cross-owner comparison helpers
 * ({@link Household}, {@link OwnerAddress#normalize}): surrounding and internal whitespace is collapsed
 * to a single space and the value is lower-cased locale-independently, so "Sydney" / " sydney " and
 * "New  York" / "New York" count as the same city.
 *
 * <p>Unlike address and telephone, the city is not stored in a canonical form, so this is only a
 * comparison aid; there is a single consumer ({@link CheckCityCapacity}) and no stored value to drift
 * from.
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
}
