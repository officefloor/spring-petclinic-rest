package org.springframework.samples.petclinic.util;

/**
 * Version 2 of the owner identity. A single home for the version number so the derived
 * identifiers, the owner response and the audit schema all agree on one value.
 *
 * <p>{@link #TAG} is mixed into every derived <em>identifier</em> (the region embedded in the
 * member id, the household id and the identity key) so that no value produced under version 1 is
 * ever produced again. The tag is deliberately kept out of the user-facing derived fields
 * (locality, timezone, owner segment), which stay the plain region code.
 */
public final class IdentityVersion {

    /** The current owner-identity version, surfaced as {@code apiVersion} and audit {@code schemaVersion}. */
    public static final int VERSION = 2;

    /** The version tag mixed into every derived identifier (e.g. {@code "V2"}). */
    public static final String TAG = "V" + VERSION;

    private IdentityVersion() {
    }
}
