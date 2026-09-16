package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of how owners are grouped by city: compared case-insensitively.
 * Shared by {@link AssignOwnerCustomerCode} (which derives the per-city sequence) and
 * {@link RejectFullOwnerCity} (which caps a city's population), so both count a city's
 * members identically.
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
