package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives an owner's IANA timezone name from the locality/region code using a fixed
 * region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
 * QLD-&gt;Australia/Brisbane).
 *
 * <p>Returns {@code null} when the region is absent or not one of the pinned regions, so the
 * {@code timezone} field is simply absent from the response for such owners.
 */
public final class OwnerTimezone {

    private OwnerTimezone() {
    }

    /** IANA timezone for the region, or {@code null} when the region is not in the table. */
    public static String fromRegion(String region) {
        if (region == null) {
            return null;
        }
        switch (region) {
            case "NSW":
                return "Australia/Sydney";
            case "VIC":
                return "Australia/Melbourne";
            case "QLD":
                return "Australia/Brisbane";
            default:
                return null;
        }
    }
}
