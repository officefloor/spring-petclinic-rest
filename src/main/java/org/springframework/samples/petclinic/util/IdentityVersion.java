package org.springframework.samples.petclinic.util;

/**
 * The version of the owner identity scheme. Version 2 mixes a fixed {@value #TAG} version
 * tag into every derived identifier — the {@link MemberId member id}, the
 * {@link IdentityKey identity key} and the household id — so every identifier differs from
 * its version-1 form and no value produced under version 1 is produced again.
 *
 * <p>The tag lives <em>inside</em> the identifiers only. The user-facing region ("locality")
 * is the plain region code (e.g. {@code NSW}), recovered from a version-2 region code with
 * {@link #plainRegion(String)}; it, the timezone and the owner segment's derived region
 * never carry the tag.
 */
public final class IdentityVersion {

    /** The current identity scheme version, reported as the response {@code apiVersion}. */
    public static final int CURRENT = 2;

    /** The fixed version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }

    /**
     * The region code embedded inside version-2 identifiers: the version {@link #TAG} tag
     * followed by the plain region (e.g. {@code NSW} becomes {@code V2NSW}).
     */
    public static String regionCode(String region) {
        return TAG + region;
    }

    /**
     * The plain region recovered from a version-2 region code by stripping the version
     * {@link #TAG} tag (e.g. {@code V2NSW} becomes {@code NSW}); {@code null} when the code
     * is null, and returned unchanged when it carries no tag.
     */
    public static String plainRegion(String regionCode) {
        if (regionCode == null) {
            return null;
        }
        return regionCode.startsWith(TAG) ? regionCode.substring(TAG.length()) : regionCode;
    }
}
