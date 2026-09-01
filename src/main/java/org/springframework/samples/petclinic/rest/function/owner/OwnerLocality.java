package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Region resolution for owner identity. {@link #forPostcode} maps an owner to a region code (the
 * {@code REGION} prefix of the {@code <REGION><FY><HASH8><CHK>} memberId), preferring the postcode: a
 * 4-digit postcode in a known range maps to its region (NSW 2000-2099, VIC 3000-3099, QLD
 * 4000-4099); otherwise it falls back to the city-to-region table (Sydney/NSW, Melbourne/VIC,
 * Brisbane/QLD), and anything unresolved is {@code "UNKNOWN"}. The user-facing {@code locality} is the
 * plain region code (see {@link #region}); {@link #regionV2} is the version-tagged code mixed into the
 * identifiers.
 */
public final class OwnerLocality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocality() {
    }

    /** Region for the owner's identity, derived from the postcode with the city as fallback. */
    public static String forPostcode(String postcode, String city) {
        String byPostcode = byPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** Locality: the plain region code (e.g. {@code "NSW"}), unaffected by the version tag mixed into
     *  the identifiers. This is a user-facing value, not an identifier. */
    public static String region(Owner owner) {
        return forPostcode(owner.getPostcode(), owner.getCity());
    }

    /** Version-2 region code used INSIDE the identifiers: the plain region with a fixed {@code "V2"}
     *  version tag mixed in, so no version-1 identifier value is ever produced again. */
    public static String regionV2(String postcode, String city) {
        return forPostcode(postcode, city) + "V2";
    }

    private static String byPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        if (value >= 2000 && value <= 2099) {
            return "NSW";
        }
        if (value >= 3000 && value <= 3099) {
            return "VIC";
        }
        if (value >= 4000 && value <= 4099) {
            return "QLD";
        }
        return null;
    }
}
