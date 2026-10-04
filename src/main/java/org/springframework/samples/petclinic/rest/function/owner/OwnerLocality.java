package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Single source of truth for the owner city-to-region rule shared across the {@code POST /owners}
 * pipeline: a fixed table maps Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD, and any other
 * (or null) city has no known region.
 *
 * <p>Centralised here (as {@link OwnerAddress} is for address normalisation) so the read-time and
 * write-time sides of the rule cannot drift: {@code OwnerMapper.locality} derives the returned
 * {@code locality} from it, and {@link ValidateOwnerPostcode} uses it to decide which region's
 * postcode range a supplied postcode must fall within.
 */
public final class OwnerLocality {

    /** City -> canonical region. Anything not listed has no known region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocality() {
    }

    /**
     * The owner's canonical region for the given city, or {@code null} when the city is not in the
     * table (no known region). Callers that need a display sentinel map {@code null} themselves
     * (e.g. to {@code "UNKNOWN"}).
     */
    public static String regionFor(String city) {
        return city == null ? null : CITY_REGION.get(city);
    }
}
