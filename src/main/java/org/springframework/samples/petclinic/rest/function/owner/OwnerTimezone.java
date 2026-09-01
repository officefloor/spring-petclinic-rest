package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Region-to-timezone lookup for owner identity. {@link #of} reads the owner's
 * locality/region (see {@link OwnerLocality#region}) and maps it to an IANA timezone via the
 * fixed table NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane; any other
 * region has no mapping and yields {@code null}.
 */
public final class OwnerTimezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private OwnerTimezone() {
    }

    /** IANA timezone for the owner's region, or {@code null} when the region has no mapping. */
    public static String of(Owner owner) {
        return REGION_TIMEZONE.get(OwnerLocality.region(owner));
    }
}
