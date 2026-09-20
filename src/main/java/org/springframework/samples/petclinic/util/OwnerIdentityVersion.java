package org.springframework.samples.petclinic.util;

/**
 * Version 2 of the owner identity. Every identifier — the {@code memberId}, {@code householdId}
 * and {@code identityKey} — is derived with a version-2 algorithm that mixes in the fixed
 * {@value #TAG} tag, so each V2 identifier differs from its version-1 form and no version-1
 * value is ever reproduced. For the {@code memberId} the tag rides on the {@link #regionCode(String)
 * region code} embedded in the id; for the hash-based {@code householdId} and {@code identityKey}
 * it is mixed into the hashed material.
 *
 * <p>The tag is an <em>identifier</em> concern only: it never appears in the user-facing region.
 * The plain region (e.g. {@code "NSW"}, see {@link CityLocality}) still backs the
 * {@code locality}, the {@code timezone} and the owner segment's derived region.
 *
 * <p>This class owns the version constants and the {@value #TAG} tag so every consumer stays in
 * step: the identifier derivations, the {@code apiVersion} on the owner response and the
 * {@code schemaVersion} on the owner-created audit event.
 */
public final class OwnerIdentityVersion {

    /** The owner-identity API version, surfaced as the owner response's {@code apiVersion}. */
    public static final int API_VERSION = 2;

    /** The owner-created audit event schema version, surfaced as its {@code schemaVersion}. */
    public static final int AUDIT_SCHEMA_VERSION = 2;

    /** The fixed version tag mixed into every V2 identifier. */
    public static final String TAG = "V2";

    private OwnerIdentityVersion() {
    }

    /**
     * The versioned region code embedded inside the {@code memberId}: the plain {@code region}
     * with the {@value #TAG} tag mixed in (e.g. {@code "V2NSW"}). This is an identifier-internal
     * value only — the user-facing region stays the plain code.
     *
     * @param region the plain canonical region, as resolved by {@link CityLocality}
     * @return the tagged region code for use inside the member-id
     */
    public static String regionCode(String region) {
        return TAG + region;
    }
}
