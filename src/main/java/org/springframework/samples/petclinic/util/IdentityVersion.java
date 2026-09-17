package org.springframework.samples.petclinic.util;

/**
 * Version 2 of the owner identity. Every identifier the owner carries — the {@link MemberId member
 * id}, the household id and the identity key — is rederived by mixing in the fixed version
 * {@link #TAG tag}, so no value produced under version 1 is produced again. Two shapes of mixing are
 * offered because the identifiers combine the tag differently:
 * <ul>
 * <li>{@link #regionCode(String)} tags the <em>region</em> embedded inside the member id, and
 * {@link #plainRegion(String)} recovers the plain region back from it;</li>
 * <li>{@link #tagged(String)} tags a <em>hash input</em>, used by the household id and identity key.</li>
 * </ul>
 * The tag lives only inside the identifiers: the user-facing locality, timezone and owner segment are
 * derived from the {@link #plainRegion(String) plain region}, so they keep reporting the bare region
 * code (e.g. {@code NSW}). Pure functions with no dependency on other owners, exposed as plain helpers.
 */
public final class IdentityVersion {

    /** The identity version, reported to clients as the owner response {@code apiVersion}. */
    public static final int VERSION = 2;

    /** The fixed version tag mixed into every identifier so no version-1 value recurs. */
    private static final String TAG = "V" + VERSION;

    private IdentityVersion() {
    }

    /**
     * The region code embedded inside version-2 identifiers: the plain region with the version
     * {@link #TAG tag} appended (e.g. {@code NSW} -> {@code NSWV2}). {@code null} stays {@code null}.
     */
    public static String regionCode(String plainRegion) {
        return plainRegion == null ? null : plainRegion + TAG;
    }

    /**
     * Recovers the plain region from a {@link #regionCode(String) version-2 region code} by dropping
     * the trailing version {@link #TAG tag}. A value without the tag (or {@code null}) is returned
     * unchanged, so it is safe to call on any region string.
     */
    public static String plainRegion(String regionCode) {
        if (regionCode == null || !regionCode.endsWith(TAG)) {
            return regionCode;
        }
        return regionCode.substring(0, regionCode.length() - TAG.length());
    }

    /**
     * Mixes the version {@link #TAG tag} into a hash input, prefixing it and a separator so a
     * version-2 digest can never coincide with the version-1 digest of the same value.
     */
    public static String tagged(String input) {
        return TAG + "|" + (input == null ? "" : input);
    }
}
