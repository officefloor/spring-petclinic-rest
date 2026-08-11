package org.springframework.samples.petclinic.mapper;

/**
 * Derives the region code that is embedded <em>inside</em> owner identifiers, at version 2. Version 2
 * rederives the region by mixing in the fixed {@link #VERSION_TAG} {@code 'V2'} tag: the identity
 * region is the tag followed by the plain region (e.g. {@code 'V2NSW'}), so every identifier that
 * embeds it changes and no value produced under version 1 — whose regions were the bare
 * {@code 'NSW'}/{@code 'VIC'}/{@code 'QLD'}/{@code 'UNKNOWN'} — is ever produced again.
 *
 * <p>This tag lives only inside identifiers. The user-facing region ({@link Locality}, and the
 * {@link Timezone} and {@link OwnerSegment} derived from it) stays the plain, un-tagged region.
 * Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit mapping
 * method.
 */
public final class RegionCode {

    /** The fixed version-2 tag mixed into the region inside identifiers. */
    public static final String VERSION_TAG = "V2";

    private RegionCode() {
    }

    /**
     * The version-2 region embedded inside identifiers for the given {@code city} and {@code postcode}:
     * the {@code 'V2'} tag prefixed to the plain region from {@link Locality}.
     */
    public static String identityRegion(String city, String postcode) {
        return VERSION_TAG + Locality.of(city, postcode);
    }
}
