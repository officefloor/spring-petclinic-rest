package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's locality (region) from its city using a fixed city-to-region table
 * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD). Any city not listed yields "UNKNOWN".
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
