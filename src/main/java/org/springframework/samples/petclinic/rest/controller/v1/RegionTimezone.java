package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Maps an owner's locality/region to its IANA timezone name via a fixed table
 * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane).
 */
public final class RegionTimezone {

    private RegionTimezone() {
    }

    /** The IANA timezone name for {@code owner}'s region, or {@code null} when the region has none. */
    public static String of(Owner owner) {
        return switch (CustomerCodes.region(owner)) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }
}
