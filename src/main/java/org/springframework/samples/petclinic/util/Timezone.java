package org.springframework.samples.petclinic.util;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code timezone} as an IANA name from the region encoded in the
 * owner's identity. The region is resolved through the shared {@link MemberId#regionOf(Owner)}
 * derivation (so it matches the reported {@code locality}) and mapped through the fixed
 * region-to-timezone table: NSW -&gt; Australia/Sydney, VIC -&gt; Australia/Melbourne,
 * QLD -&gt; Australia/Brisbane. Any region outside the table resolves to {@code null}.
 */
public final class Timezone {

    /** Region -> IANA timezone name; anything not listed resolves to {@code null}. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /**
     * The IANA timezone name for {@code owner}, derived from its region.
     *
     * @param owner the owner whose timezone to derive.
     * @return the IANA timezone name, or {@code null} when the region is not in the table.
     */
    public static String of(Owner owner) {
        return REGION_TIMEZONE.get(MemberId.regionOf(owner));
    }
}
