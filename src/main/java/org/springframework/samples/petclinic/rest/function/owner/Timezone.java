package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * IANA timezone for a pet owner, derived from the owner's canonical region (see {@link Locality})
 * using a fixed region-to-timezone table: {@code NSW -> Australia/Sydney},
 * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}. A region not in the table
 * (e.g. {@code UNKNOWN}) yields {@code null} so the field is absent on responses. Derived purely
 * from the owner's own state via {@link Locality}, so it carries no stored data and is
 * seed-independent. Used by the owner mapper to expose {@code timezone} on responses.
 */
public final class Timezone {

    /** Region -> IANA timezone. Fixed, pinned reference data. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /**
     * The IANA timezone for the given owner, resolved from its {@link Locality}. Returns {@code null}
     * when the region is not one of the known regions.
     */
    public static String of(Owner owner) {
        return REGION_TIMEZONE.get(Locality.of(owner));
    }

    /**
     * The IANA timezone for an owner, resolved from the region derived by
     * {@link Locality#of(String, String, String)} (member id, then postcode, then city).
     * Returns {@code null} when the region is not one of the known regions.
     */
    public static String of(String memberId, String postcode, String city) {
        return REGION_TIMEZONE.get(Locality.of(memberId, postcode, city));
    }
}
