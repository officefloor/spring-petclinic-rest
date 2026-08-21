package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Version-2 constants for the owner identity. Version 2 rederives the region code that goes
 * <em>inside</em> the identifiers ({@code memberId}, {@code identityKey} and {@code householdId})
 * by mixing in the fixed {@link #TAG version tag {@code "V2"}}, so every identifier changes and no
 * value produced under version 1 is produced again.
 *
 * <p>The tag only ever appears inside those identifiers. User-facing derived fields — the
 * {@code locality}, {@code timezone} and the owner segment's region — stay the plain region code
 * (e.g. {@code "NSW"}); {@link #stripTag(String)} removes the tag when a plain region is recovered
 * from an identifier.
 */
public final class OwnerIdentityVersion {

    /** The fixed version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    /** The top-level {@code apiVersion} reported on the owner response. */
    public static final int API_VERSION = 2;

    /** The {@code schemaVersion} stamped on the structured owner-created audit event. */
    public static final int AUDIT_SCHEMA_VERSION = 2;

    private OwnerIdentityVersion() {
    }

    /**
     * @return {@code region} with a trailing {@link #TAG} removed (so a plain region code such as
     *         {@code "NSW"} is recovered from an identifier's {@code "NSWV2"} region), or the value
     *         unchanged when it is {@code null} or carries no tag
     */
    public static String stripTag(String region) {
        if (region == null) {
            return null;
        }
        return region.endsWith(TAG) ? region.substring(0, region.length() - TAG.length()) : region;
    }
}
