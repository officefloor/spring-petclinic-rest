package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's IANA timezone from their locality/region via a fixed
 * region-to-timezone table.
 *
 * <p>The table pins each known region to its IANA name (NSW -&gt; Australia/Sydney,
 * VIC -&gt; Australia/Melbourne, QLD -&gt; Australia/Brisbane). A region outside the
 * table &mdash; including the {@code "UNKNOWN"} locality &mdash; yields {@code "UNKNOWN"}.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for an implicit {@code String -> String} mapping method and apply it to
 * every string property, mirroring {@link Locality}.
 */
public final class Timezone {

    /** Fixed region -&gt; IANA timezone table. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /** The IANA timezone for {@code region}, or {@code "UNKNOWN"} when it is not in the table. */
    public static String of(String region) {
        return region == null ? "UNKNOWN" : REGION_TIMEZONE.getOrDefault(region, "UNKNOWN");
    }

    /**
     * The IANA timezone for the region carried by a {@code '<REGION><FY><HASH8><CHK>'} member id.
     * The region is resolved through {@link Locality#ofMemberId(String)}, so the timezone derives
     * from the same single source of an owner's locality.
     */
    public static String ofMemberId(String memberId) {
        return of(Locality.ofMemberId(memberId));
    }
}
