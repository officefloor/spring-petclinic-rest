package org.springframework.samples.petclinic.service;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code locality} is the canonical region derived from its city using a
 * fixed city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), or {@code UNKNOWN} when
 * the city is not in the table. Kept as a small, self-contained unit so the rule can be applied from
 * the read flow without adding complexity to the mapper, controller, or service.
 */
public final class OwnerLocalityPolicy {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocalityPolicy() {
    }

    /**
     * Derive the {@code locality} for the given owner.
     *
     * @param owner the owner whose locality to derive
     * @return the canonical region for the owner's city, or {@code "UNKNOWN"} when the city is
     *     not in the fixed city-to-region table
     */
    public static String locality(Owner owner) {
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }
}
