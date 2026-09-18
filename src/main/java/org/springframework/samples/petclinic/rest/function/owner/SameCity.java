package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of two owners being in the same city, compared case-insensitively
 * (leading and trailing whitespace ignored). Provides the membership test used to count how
 * many existing owners already live in a new owner's city.
 */
final class SameCity {

    private SameCity() {
    }

    /** Whether {@code owner} is in {@code city}. */
    static boolean matches(Owner owner, String city) {
        return normalize(owner.getCity()).equals(normalize(city));
    }

    /** How many of {@code owners} are in {@code city}. */
    static int count(Iterable<Owner> owners, String city) {
        int inCity = 0;
        for (Owner owner : owners) {
            if (matches(owner, city)) {
                inCity++;
            }
        }
        return inCity;
    }

    /** A city name trimmed and lower-cased, so comparison is case-insensitive. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
