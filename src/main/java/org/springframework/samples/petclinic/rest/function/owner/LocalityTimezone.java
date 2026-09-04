package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The IANA timezone name for an owner's locality. The locality (region) is the one derived by
 * {@link OwnerRegion#of(Owner)}; it is mapped through a fixed region-to-timezone table
 * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane). Regions absent
 * from the table (including the 'UNKNOWN' default) have no timezone and resolve to {@code null}.
 */
public final class LocalityTimezone {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private LocalityTimezone() {
    }

    /** The IANA timezone for {@code owner}'s locality, or {@code null} when its region has none. */
    public static String of(Owner owner) {
        return REGION_TIMEZONE.get(OwnerRegion.of(owner));
    }
}
