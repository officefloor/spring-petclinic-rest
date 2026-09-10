package org.springframework.samples.petclinic.rest.function.common;

/**
 * The single source of truth for the owner identity contract version. Version 2 rederives every
 * identifier (memberId, identityKey, householdId) by mixing the fixed {@link #TAG 'V2'} version
 * tag into the hash inputs, so no value produced under version 1 is produced again.
 *
 * <p>The tag is mixed in only <em>inside</em> the identifiers. The user-facing region code — the
 * {@code locality}, the {@code timezone} and the owner segment's derived region — stays the plain
 * region (e.g. {@code NSW}) and never carries the tag.
 */
public final class IdentityVersion {

    /** The fixed version tag mixed into the version-2 identifier hashes. */
    public static final String TAG = "V2";

    /** The owner API response contract version carried by {@code apiVersion}. */
    public static final int API_VERSION = 2;

    /** The audit event schema version carried by {@code schemaVersion}. */
    public static final int SCHEMA_VERSION = 2;

    private IdentityVersion() {
    }
}
