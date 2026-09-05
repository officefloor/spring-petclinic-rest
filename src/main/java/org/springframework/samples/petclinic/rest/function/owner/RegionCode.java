package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the version-2 region code mixed INSIDE the owner's identifiers (the {@code memberId},
 * {@code householdId} and {@code identityKey}). The version-2 algorithm takes the plain region code
 * (see {@link Locality}) and mixes in a fixed {@code "V2"} version tag, so every identifier that
 * embeds it changes and no value produced under version 1 (which carried no tag) is produced again.
 *
 * <p>This is deliberately distinct from the user-facing {@code locality}: {@code locality},
 * {@code timezone} and the owner segment's derived region stay the plain region code (e.g. {@code
 * "NSW"}) — the {@code "V2"} tag appears only inside the identifiers, never in those fields.
 */
public final class RegionCode {

    /** The fixed version tag mixed into every version-2 region code. */
    static final String VERSION_2_TAG = "V2";

    private RegionCode() {
    }

    /**
     * The version-2 region code for the {@code memberId}: the postcode-or-city region (see
     * {@link Locality#of(String, String)}) with the {@code "V2"} tag mixed in, e.g. {@code "NSWV2"}.
     */
    public static String v2(String city, String postcode) {
        return Locality.of(city, postcode) + VERSION_2_TAG;
    }

    /**
     * The version-2 region code derived from the postcode alone (city-independent), e.g. {@code
     * "NSWV2"} or {@code "UNKNOWNV2"}. Used by the hashing identifiers ({@code householdId},
     * {@code identityKey}) so owners sharing a postcode share the same region component regardless of
     * the city they entered.
     */
    public static String v2FromPostcode(String postcode) {
        String region = PostcodeRanges.regionFor(postcode);
        return (region == null ? Locality.UNKNOWN : region) + VERSION_2_TAG;
    }
}
