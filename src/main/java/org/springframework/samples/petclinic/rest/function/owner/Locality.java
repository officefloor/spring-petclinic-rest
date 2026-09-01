package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's locality from the pinned city-to-region table
 * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), or {@code UNKNOWN}
 * for any other city.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    public static String of(Owner owner) {
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }
}
