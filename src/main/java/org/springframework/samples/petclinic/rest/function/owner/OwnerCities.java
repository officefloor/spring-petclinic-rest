package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of how owners are grouped by city: compared case-insensitively.
 * Used by {@link RejectFullOwnerCity} (which caps a city's population) to count a city's
 * members.
 */
final class OwnerCities {

    private OwnerCities() {
    }

    /** The number of existing owners in {@code city}. */
    static int size(Iterable<Owner> owners, String city) {
        int count = 0;
        for (Owner existing : owners) {
            if (city != null && city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        return count;
    }
}
