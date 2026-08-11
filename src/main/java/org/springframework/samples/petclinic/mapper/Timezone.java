package org.springframework.samples.petclinic.mapper;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code timezone}: the IANA name for the owner's locality/region via the fixed
 * region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane).
 * The region is read from {@link Locality}, so timezone always agrees with the reported locality.
 * Returns {@code null} when the region resolves to no table entry (e.g. "UNKNOWN"). Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit mapping method.
 */
public final class Timezone {

    /** Region -> IANA timezone; anything not listed derives no timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /** The IANA timezone for {@code owner}, or {@code null} when its region has no table entry. */
    public static String of(Owner owner) {
        return REGION_TIMEZONE.get(Locality.of(owner));
    }
}
