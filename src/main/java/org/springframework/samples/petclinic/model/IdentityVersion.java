package org.springframework.samples.petclinic.model;

/**
 * The single definition of the owner identity <em>version</em> facts shared across the identity
 * derivation, the owner response and the audit event.
 *
 * <p>Version 2 rederives every owner identifier — the region code used inside the identifiers, the
 * {@code householdId}, the {@code identityKey} and the {@code memberId} — with an algorithm that
 * mixes in the fixed {@link #TAG version tag}, so no value produced under version 1 is produced
 * again. The tag is an <em>identifier-only</em> concern: it appears only inside those derived
 * identifiers, never in the user-facing {@code locality}, {@code timezone} or the owner segment's
 * derived region, which all keep the plain region code (e.g. {@code NSW}).
 *
 * <p>Lives in {@code model} beside {@link Owner} — the same home as {@link OwnerIdentity} and
 * {@link CityRegions} — so both the mapping and request-handling layers can depend on it without a
 * package cycle.
 */
public final class IdentityVersion {

    private IdentityVersion() {
    }

    /** The fixed version tag mixed into every version-2 identifier derivation. */
    public static final String TAG = "V2";

    /** The API version stamped on the owner response ({@code apiVersion}). */
    public static final int API_VERSION = 2;

    /** The schema version stamped on the audit event ({@code schemaVersion}). */
    public static final int SCHEMA_VERSION = 2;
}
