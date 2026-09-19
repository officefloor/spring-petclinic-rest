package org.springframework.samples.petclinic.model;

/**
 * Version 2 of the owner identity derivation. Every owner identifier &mdash; the {@link MemberId member
 * id}, the household id and the {@link Owner#getIdentityKey() identity key} &mdash; mixes in the fixed
 * {@link #TAG version tag} so that no value produced under version 1 is ever produced again.
 *
 * <p>The tag is confined to the identifiers. The user-facing {@link Owner#getLocality() locality}
 * (and, through it, the {@link Owner#getTimezone() timezone} and {@link Owner#getOwnerSegment() owner
 * segment}) keep using the plain region code: the tagged region embedded inside the member id is read
 * back out with the tag stripped (see {@link MemberId#regionOf(String)}), so the region seen inside an
 * identifier is version-2 while the region seen by clients stays plain (e.g. {@code "NSW"}).
 */
public final class IdentityVersion {

    /** The fixed version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    /** The API version advertised in the owner response. */
    public static final int API_VERSION = 2;

    private IdentityVersion() {
    }

    /**
     * The region code as it appears INSIDE a version-2 identifier: the plain {@code region} prefixed
     * with the {@link #TAG version tag}. Because a plain region never begins with the tag, a version-2
     * identifier can never coincide with its version-1 form.
     */
    public static String taggedRegion(String region) {
        return TAG + region;
    }

    /**
     * The plain region recovered from a {@link #taggedRegion(String) tagged region} by stripping the
     * {@link #TAG version tag}. A value without the tag (e.g. a legacy version-1 region) is returned
     * unchanged, so reading back a plain region is always safe.
     */
    public static String plainRegion(String taggedRegion) {
        return (taggedRegion != null && taggedRegion.startsWith(TAG))
                ? taggedRegion.substring(TAG.length())
                : taggedRegion;
    }

    /**
     * A version-2 hash input: {@code value} prefixed with the {@link #TAG version tag} and a separator,
     * so a digest taken over it never reproduces the untagged version-1 digest.
     */
    public static String taggedInput(String value) {
        return TAG + '|' + value;
    }
}
