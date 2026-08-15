package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fixed region-to-timezone lookup used to derive an owner's 'timezone' from its locality/region
 * (see {@link Localities}). The mapping is pinned and expressed as IANA names: NSW ->
 * Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane. A region not in the
 * table (locality "UNKNOWN") has no timezone.
 */
public final class Timezones {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private Timezones() {
    }

    /** The IANA timezone for {@code region}, or {@code null} when the region has no known zone. */
    public static String of(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /**
     * The owner's IANA timezone, derived from its locality/region (which prefers the postcode,
     * falling back to the city, see {@link CustomerCodes#localityOf(Owner)}). {@code null} when the
     * region is "UNKNOWN".
     */
    public static String ofOwner(Owner owner) {
        return of(CustomerCodes.localityOf(owner));
    }
}
